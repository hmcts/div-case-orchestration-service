package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseCreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.LinkBulkCaseWorkflow;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;

@Component
@AllArgsConstructor
@Slf4j
public class BulkCaseService implements ApplicationListener<BulkCaseCreateEvent> {

    private LinkBulkCaseWorkflow linkBulkCaseWorkflow;

    @Override
    public void onApplicationEvent(BulkCaseCreateEvent event) {
        Map<String, Object> caseResponse = event.getCaseDetails();
        final String bulkCaseId = caseResponse.get(OrchestrationConstants.ID).toString();
        Map<String, Object> bulkCaseData = (Map<String, Object>) caseResponse.get(CCD_CASE_DATA_FIELD);
        List<Map<String, Object>> divorceCaseList = (List<Map<String, Object>>) bulkCaseData.get("CaseList");

        divorceCaseList.forEach(caseElem -> {
            try {
                linkBulkCaseWorkflow.run(caseElem,bulkCaseId, null );
            } catch (Exception e) {
                //TODO this will be handle on DIV-4811
                log.error("Case update failed : {} for bulk case id {", caseElem, bulkCaseId, e );
            }

        });
        log.info("Completed bulk case process with bulk cased Id: {}", bulkCaseId);

    }
}
