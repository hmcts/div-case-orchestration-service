package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_DRAFT_KEY;

@Component
public class RetrieveDraft implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;
    private final boolean checkCcd;

    @Autowired
    public RetrieveDraft(CaseMaintenanceClient caseMaintenanceClient,
                         @Value("${draft.api.ccd.check.enabled}")  String checkCcdEnabled) {
        this.caseMaintenanceClient = caseMaintenanceClient;
        this.checkCcd = Boolean.valueOf(checkCcdEnabled);
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> noPayLoad) {
        Map<String, Object> caseData = null;

        CaseDetails cmsContent = caseMaintenanceClient
                .retrievePetition(context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString(), checkCcd);
        if (cmsContent != null && cmsContent.getCaseData() != null && !cmsContent.getCaseData().isEmpty()) {
            boolean isDraft = StringUtils.isEmpty(cmsContent.getCaseId());
            caseData = cmsContent.getCaseData();
            caseData.put(IS_DRAFT_KEY, isDraft);
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
