package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;

@Component
public class GetCaseWithId implements Task<UserDetails> {
    private final CaseMaintenanceClient caseMaintenanceClient;

    private final AuthUtil authUtil;

    @Autowired
    public GetCaseWithId(CaseMaintenanceClient caseMaintenanceClient, AuthUtil authUtil) {
        this.caseMaintenanceClient = caseMaintenanceClient;
        this.authUtil = authUtil;
    }

    @Override
    public UserDetails execute(TaskContext context, UserDetails payload) throws TaskException {
        CaseDetails caseDetails;

        final String caseWorkerToken = authUtil.getCaseworkerToken();
        final String caseId = String.valueOf(context.getTransientObject(CASE_ID_JSON_KEY));
        caseDetails = caseMaintenanceClient.retrievePetitionById(
            caseWorkerToken,
            caseId
        );

        if (caseDetails == null) {
            throw new TaskException(
                new CaseNotFoundException(String.format("No case found with ID [%s]", caseId))
            );
        }

        context.setTransientObject(CCD_CASE_DATA, caseDetails.getCaseData());

        return null;
    }
}