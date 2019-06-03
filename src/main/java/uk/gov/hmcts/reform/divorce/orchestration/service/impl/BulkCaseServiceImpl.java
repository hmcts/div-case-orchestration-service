package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseCreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseUpdateCourtHearingEvent;
import uk.gov.hmcts.reform.divorce.orchestration.exception.BulkUpdateException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkCaseService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkBulkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateBulkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateCourtHearingDetailsWorkflow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_ACCEPTED_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.LISTED_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;

@Service
@AllArgsConstructor
@Slf4j
public class BulkCaseServiceImpl implements BulkCaseService {

    private final LinkBulkCaseWorkflow linkBulkCaseWorkflow;
    private final UpdateCourtHearingDetailsWorkflow updateCourtHearingDetailsWorkflow;
    private final UpdateBulkCaseWorkflow updateBulkCaseWorkflow;

    @Override
    @EventListener
    public void handleBulkCaseCreateEvent(BulkCaseCreateEvent event) {
        long startTime = Instant.now().toEpochMilli();
        TaskContext context = (TaskContext) event.getSource();
        Map<String, Object> caseResponse = event.getCaseDetails();
        final String bulkCaseId =  String.valueOf(caseResponse.get(OrchestrationConstants.ID));

        Map<String, Object> bulkCaseData = (Map<String, Object>) caseResponse.getOrDefault(CCD_CASE_DATA_FIELD, Collections.emptyMap());
        List<Map<String, Object>> divorceCaseList = (List<Map<String, Object>>) bulkCaseData.getOrDefault(CASE_LIST_KEY, Collections.emptyList());

        divorceCaseList.forEach(caseElem -> {
            try {
                linkBulkCaseWorkflow.run(caseElem, bulkCaseId, context.getTransientObject(AUTH_TOKEN_JSON_KEY));
            } catch (Exception e) {
                //TODO this will be handled on DIV-4811
                log.error("Case update failed : for bulk case id {}", bulkCaseId, e );
            }

        });

        long endTime = Instant.now().toEpochMilli();
        log.info("Completed bulk case process with bulk cased Id:{} in:{} millis", bulkCaseId, endTime - startTime);

    }

    @Override
    @EventListener
    public void handleBulkCaseUpdateCourtHearingEvent(BulkCaseUpdateCourtHearingEvent event) throws WorkflowException {
        final long startTime = Instant.now().toEpochMilli();

        TaskContext context = (TaskContext) event.getSource();
        Map<String, Object> caseResponse = event.getCaseDetails();
        final String bulkCaseId = String.valueOf(caseResponse.get(OrchestrationConstants.ID));

        Map<String, Object> bulkCaseData = (Map<String, Object>) caseResponse.getOrDefault(CCD_CASE_DATA_FIELD, Collections.emptyMap());
        List<Map<String, Object>> acceptedDivorceCaseList =
                (List<Map<String, Object>>) bulkCaseData.getOrDefault(BULK_CASE_ACCEPTED_LIST_KEY, Collections.emptyList());

        if (acceptedDivorceCaseList.isEmpty()) {
            throw new BulkUpdateException("Accepted case list is empty. Not updating bulk case");
        }

        List<String> failedCasesList = new ArrayList<>();

        acceptedDivorceCaseList.forEach(caseLinkElem -> {
            Map<String, Object> caseLink = (Map<String, Object>) caseLinkElem.get(VALUE_KEY);
            String caseId = String.valueOf(caseLink.get(CASE_REFERENCE_FIELD));
            
            try {
                log.info("Updating court hearing details for case id {} in bulk case id {}", caseId, bulkCaseId);
                updateCourtHearingDetailsWorkflow.run(bulkCaseData, caseId, context.getTransientObject(AUTH_TOKEN_JSON_KEY));
            } catch (Exception e) {
                log.error("Case update with court hearing details failed : for case id {} in bulk case id {}", caseId, bulkCaseId, e);
                failedCasesList.add(caseId);
            }
        });

        if (!failedCasesList.isEmpty()) {
            log.error("List of failed cases for bulk case id {} is: {}", bulkCaseId, failedCasesList.toString());
            throw new BulkUpdateException(String.format("Failed to update court hearing details for some cases on bulk case id %s", bulkCaseId));
        }

        final long endTime = Instant.now().toEpochMilli();
        log.info("Completed bulk case update court hearing with bulk case Id:{} in:{} millis", bulkCaseId, endTime - startTime);

        log.info("Updating bulk case id {} to Listed state", bulkCaseId);
        updateBulkCaseWorkflow.run(Collections.emptyMap(), context.getTransientObject(AUTH_TOKEN_JSON_KEY), bulkCaseId, LISTED_EVENT);
        log.info("Completed bulk case id {} state update", bulkCaseId);
    }


}
