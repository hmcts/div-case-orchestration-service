package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_SESSION_EXISTING_PAYMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PAYMENTS;

@Component
public class PopulateExistingCollections implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public PopulateExistingCollections(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> sessionData) {
        CaseDetails cmsContent = caseMaintenanceClient
                .retrievePetitionById(
                        context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString(),
                        context.getTransientObject(CASE_ID_JSON_KEY).toString()
                );

        if (Objects.nonNull(cmsContent.getCaseData())) {
            sessionData.put(DIVORCE_SESSION_EXISTING_PAYMENTS, cmsContent.getCaseData().get(D_8_PAYMENTS));
        }

        return sessionData;
    }
}
