package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LINK_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getCaseLinkValue;

@Component
@Slf4j
public class ValidatedCaseLinkTask implements Task<Map<String, Object>> {
    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        String bulkCaseId = context.getTransientObject(BULK_LINK_CASE_ID);
        String caseId = getCaseId(context);

        Map<String, Object> caseData = caseDetails.getCaseData();

        if (!isCaseLinkedWithBulkCase(caseData, bulkCaseId)) {
            log.warn("Case data with caseId {} not linked with bulk with bulkCaseId {}", caseId, bulkCaseId);
            context.setTaskFailed(true);
        }

        return Collections.emptyMap();
    }

    private boolean isCaseLinkedWithBulkCase(Map<String, Object> caseData, String bulkCaseId) {
        return bulkCaseId.equals(getCaseLinkValue(caseData, BULK_LISTING_CASE_ID_FIELD));
    }
}
