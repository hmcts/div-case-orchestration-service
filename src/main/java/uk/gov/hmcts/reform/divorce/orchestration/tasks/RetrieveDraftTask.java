package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.DraftHelper.isDraft;

@Component
@RequiredArgsConstructor
public class RetrieveDraftTask implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> noPayLoad) {
        Map<String, Object> caseData = null;

        CaseDetails cmsContent = caseMaintenanceClient
            .retrievePetition(context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString());

        if (cmsContent != null && cmsContent.getCaseData() != null && !cmsContent.getCaseData().isEmpty()) {
            caseData = cmsContent.getCaseData();

            if (!isDraft(caseData)) {
                context.setTransientObject(CASE_ID_JSON_KEY, cmsContent.getCaseId());
                context.setTransientObject(CASE_STATE_JSON_KEY, cmsContent.getState());
            }
        } else {
            context.setTaskFailed(true);
        }

        return caseData;
    }
}
