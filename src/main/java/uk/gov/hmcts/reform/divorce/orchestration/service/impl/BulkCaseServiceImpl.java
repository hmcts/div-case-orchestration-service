package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseCreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkCaseService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkBulkCaseWorkflow;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;

@Service
@AllArgsConstructor
@Slf4j
public class BulkCaseServiceImpl implements BulkCaseService {

    private final LinkBulkCaseWorkflow linkBulkCaseWorkflow;

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
                //TODO this will be handle on DIV-4811
                log.error("Case update failed : for bulk case id {}", bulkCaseId, e );
            }

        });

        long endTime = Instant.now().toEpochMilli();
        log.info("Completed bulk case process with bulk cased Id:{} in:{} millis", bulkCaseId, endTime - startTime);

    }
}
