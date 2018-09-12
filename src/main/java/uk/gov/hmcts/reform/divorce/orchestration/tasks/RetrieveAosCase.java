package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CHECK_CCD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;

@Component
public class RetrieveAosCase implements Task<CaseDataResponse> {
    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public RetrieveAosCase(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public CaseDataResponse execute(TaskContext context, CaseDataResponse payload) {
        CaseDetails caseDetails = caseMaintenanceClient.retrieveAosCase(
            String.valueOf(context.getTransientObject(AUTH_TOKEN_JSON_KEY)),
            Boolean.valueOf(String.valueOf(context.getTransientObject(CHECK_CCD))));

        if (caseDetails == null) {
            context.setTaskFailed(true);
            return CaseDataResponse.builder().build();
        }

        context.setTransientObject(CCD_CASE_DATA, caseDetails.getCaseData());

        return CaseDataResponse.builder()
            .caseId(caseDetails.getCaseId())
            .state(caseDetails.getState())
            .courts(String.valueOf(caseDetails.getCaseData().get(D_8_DIVORCE_UNIT)))
            .build();
    }
}