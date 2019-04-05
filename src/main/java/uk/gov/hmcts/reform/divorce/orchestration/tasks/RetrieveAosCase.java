package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;

@Component
public class RetrieveAosCase implements Task<CaseDataResponse> {
    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public RetrieveAosCase(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public CaseDataResponse execute(TaskContext context, CaseDataResponse payload) throws TaskException {
        CaseDetails caseDetails = caseMaintenanceClient.retrieveAosCase(
            context.getTransientObject(AUTH_TOKEN_JSON_KEY)
        );

        if (caseDetails == null) {
            throw new TaskException(new CaseNotFoundException("No case found"));
        }

        context.setTransientObject(CCD_CASE_DATA, caseDetails.getCaseData());

        return CaseDataResponse.builder()
            .caseId(caseDetails.getCaseId())
            .state(caseDetails.getState())
            .courts(String.valueOf(caseDetails.getCaseData().get(D_8_DIVORCE_UNIT)))
            .build();
    }
}