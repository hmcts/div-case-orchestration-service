package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_DRAFT_KEY;

@Component
public class RetrieveDraft implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public RetrieveDraft(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> noPayLoad) {
        Map<String, Object> caseData = null;

        CaseDetails cmsContent = caseMaintenanceClient
                .retrievePetition(context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString());

        if (cmsContent != null && cmsContent.getCaseData() != null && !cmsContent.getCaseData().isEmpty()) {
            caseData = cmsContent.getCaseData();
            Boolean isDraft = Optional.ofNullable(caseData.get(IS_DRAFT_KEY))
                                .map(Boolean.class::cast)
                                .orElse(false);

            if (!isDraft) {
                context.setTransientObject(CASE_ID_JSON_KEY, cmsContent.getCaseId());
                context.setTransientObject(CASE_STATE_JSON_KEY, cmsContent.getState());
            }
        } else {
            context.setTaskFailed(true);
        }
        return caseData;
    }
}
