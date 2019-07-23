package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.BulkWorkflowExecutionResult;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseAcceptedCasesEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseCreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseUpdateCourtHearingEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseUpdatePronouncementDateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.exception.BulkUpdateException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkCaseService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkBulkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RemoveBulkCaseLinkWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateBulkCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateCourtHearingDetailsWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdatePronouncementDateWorkflow;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_ACCEPTED_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CREATE_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.LISTED_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.PRONOUNCED_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.REMOVED_CASE_LIST;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkCaseServiceImpl implements BulkCaseService {

    private final LinkBulkCaseWorkflow linkBulkCaseWorkflow;
    private final UpdateCourtHearingDetailsWorkflow updateCourtHearingDetailsWorkflow;
    private final UpdateBulkCaseWorkflow updateBulkCaseWorkflow;
    private final UpdatePronouncementDateWorkflow updatePronouncementDateWorkflow;
    private final RemoveBulkCaseLinkWorkflow removeBulkCaseLinkWorkflow;

    @Override
    @EventListener
    public void handleBulkCaseCreateEvent(BulkCaseCreateEvent event) throws WorkflowException {
        final long startTime = Instant.now().toEpochMilli();
        TaskContext context = (TaskContext) event.getSource();
        Map<String, Object> caseResponse = event.getCaseDetails();
        final String bulkCaseId = String.valueOf(caseResponse.get(OrchestrationConstants.ID));

        BulkWorkflowExecutionResult result =
            linkBulkCaseWorkflow.executeWithRetriesForCreate(caseResponse, bulkCaseId, context.getTransientObject(AUTH_TOKEN_JSON_KEY));

        if (!result.isSuccessStatus()) {
            throw new BulkUpdateException(String.format("Failed to updating bulk case link for some cases on bulk case id %s", bulkCaseId));
        }

        Set<String> removableCaseIds = Optional.ofNullable(result.getRemovableCaseIds()).orElse(Collections.emptySet());
        Map<String, Object> updatePayload = new HashMap<>();

        if (removableCaseIds.size() > 0) {
            updatePayload.put(CASE_LIST_KEY, filterBulkCases(caseResponse, result.getRemovableCaseIds()));
            updatePayload.put(BULK_CASE_ACCEPTED_LIST_KEY, filterAcceptedCases(caseResponse, result.getRemovableCaseIds()));
        }

        final long endTime = Instant.now().toEpochMilli();
        updateBulkCaseWorkflow.run(updatePayload, context.getTransientObject(AUTH_TOKEN_JSON_KEY), bulkCaseId, CREATE_EVENT);
        log.info("Completed bulk case process with bulk cased Id:{} in:{} millis", bulkCaseId, endTime - startTime);
    }

    @Override
    @EventListener
    public void handleBulkCaseUpdateCourtHearingEvent(BulkCaseUpdateCourtHearingEvent event) throws WorkflowException {
        final long startTime = Instant.now().toEpochMilli();

        TaskContext context = (TaskContext) event.getSource();
        Map<String, Object> caseResponse = event.getCaseDetails();
        final String bulkCaseId = String.valueOf(caseResponse.get(OrchestrationConstants.ID));

        boolean success = updateCourtHearingDetailsWorkflow.executeWithRetries(caseResponse,
            bulkCaseId, context.getTransientObject(AUTH_TOKEN_JSON_KEY));

        if (!success) {
            throw new BulkUpdateException(String.format("Failed to update court hearing details for some cases on bulk case id %s", bulkCaseId));
        }

        final long endTime = Instant.now().toEpochMilli();
        log.info("Completed bulk case update court hearing with bulk case Id:{} in:{} millis", bulkCaseId, endTime - startTime);

        log.info("Updating bulk case id {} to Listed state", bulkCaseId);
        updateBulkCaseWorkflow.run(Collections.emptyMap(), context.getTransientObject(AUTH_TOKEN_JSON_KEY), bulkCaseId, LISTED_EVENT);
        log.info("Completed bulk case id {} state update", bulkCaseId);
    }

    @Override
    @EventListener
    public void handleBulkCaseUpdatePronouncementDateEvent(BulkCaseUpdatePronouncementDateEvent event) throws WorkflowException {
        final long startTime = Instant.now().toEpochMilli();

        TaskContext context = (TaskContext) event.getSource();
        Map<String, Object> caseResponse = event.getCaseDetails();
        final String bulkCaseId = String.valueOf(caseResponse.get(OrchestrationConstants.ID));

        boolean success = updatePronouncementDateWorkflow.executeWithRetries(caseResponse,
                bulkCaseId, context.getTransientObject(AUTH_TOKEN_JSON_KEY));

        if (!success) {
            throw new BulkUpdateException(String.format("Failed to update court pronouncement date for some cases on bulk case id %s", bulkCaseId));
        }

        final long endTime = Instant.now().toEpochMilli();
        log.info("Completed bulk case update pronouncement date with bulk case Id:{} in:{} millis", bulkCaseId, endTime - startTime);

        log.info("Updating bulk case id {} to Pronounced state", bulkCaseId);
        updateBulkCaseWorkflow.run(Collections.emptyMap(), context.getTransientObject(AUTH_TOKEN_JSON_KEY), bulkCaseId, PRONOUNCED_EVENT);
        log.info("Completed bulk case id {} pronounced state update", bulkCaseId);
    }
  
    private List<Map<String, Object>> filterBulkCases(Map<String, Object> bulkCaseDetails, Set<String> removableCaseIds) {
        Map<String, Object> bulkCaseData = (Map<String, Object>) bulkCaseDetails.getOrDefault(CCD_CASE_DATA_FIELD, Collections.emptyMap());
        List<Map<String, Object>> bulkCaseList =
                (List<Map<String, Object>>) bulkCaseData.getOrDefault(CASE_LIST_KEY, Collections.emptyList());

        return bulkCaseList.stream().filter(caseElem -> {
            Map<String, Object> caseData = (Map<String, Object>) caseElem.get(VALUE_KEY);
            Map<String, Object> caseLink = (Map<String, Object>) caseData.get(CASE_REFERENCE_FIELD);
            return !removableCaseIds.contains(String.valueOf(caseLink.get(CASE_REFERENCE_FIELD)));
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> filterAcceptedCases(Map<String, Object> bulkCaseDetails, Set<String> removableCaseIds) {
        Map<String, Object> bulkCaseData = (Map<String, Object>) bulkCaseDetails.getOrDefault(CCD_CASE_DATA_FIELD, Collections.emptyMap());
        List<Map<String, Object>> acceptedDivorceCaseList =
                (List<Map<String, Object>>) bulkCaseData.getOrDefault(BULK_CASE_ACCEPTED_LIST_KEY, Collections.emptyList());

        return acceptedDivorceCaseList.stream().filter(caseElem -> {
            Map<String, Object> caseLink = (Map<String, Object>) caseElem.get(VALUE_KEY);
            return !removableCaseIds.contains(String.valueOf(caseLink.get(CASE_REFERENCE_FIELD)));
        }).collect(Collectors.toList());
    }

    @Override
    @EventListener
    public void handleBulkCaseAcceptedCasesEvent(BulkCaseAcceptedCasesEvent event) {
        final long startTime = Instant.now().toEpochMilli();

        TaskContext context = (TaskContext) event.getSource();
        CaseDetails caseResponse = event.getCaseDetails();
        List<String> casesToUnlink = context.getTransientObject(REMOVED_CASE_LIST);
        final String bulkCaseId = caseResponse.getCaseId();
        for (String caseId : casesToUnlink) {
            try {
                removeBulkCaseLinkWorkflow.run(caseResponse.getCaseData(), caseId, bulkCaseId, context.getTransientObject(AUTH_TOKEN_JSON_KEY));
            } catch (WorkflowException e) {
                log.error("Error removing bulk case link with bulkCaseId: {} and caseId {}", bulkCaseId, caseId);
            }
        }

        final long endTime = Instant.now().toEpochMilli();
        log.info("Completed bulk case removed link from cases with bulk case Id:{} in:{} millis", bulkCaseId, endTime - startTime);
    }
}
