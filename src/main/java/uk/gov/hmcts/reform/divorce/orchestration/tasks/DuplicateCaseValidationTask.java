package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_HWF_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow.SELECTED_COURT;

@Component
@Slf4j
public class DuplicateCaseValidationTask implements Task<Map<String, Object>> {

    @Autowired
    private CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    private TaskCommons taskCommons;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        try {
            String transientObject = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
            CaseDetails caseDetails = caseMaintenanceClient.getCase(transientObject);

            if (caseDetails != null && (
                    AWAITING_PAYMENT.equalsIgnoreCase(caseDetails.getState())
                            || AWAITING_HWF_DECISION.equalsIgnoreCase(caseDetails.getState()))) {
                payload.put(ID, caseDetails.getCaseId());

                String selectCourtId = (String) caseDetails.getCaseData().get(D_8_DIVORCE_UNIT);
                Court selectCourt = taskCommons.getCourt(selectCourtId);
                context.setTransientObject(SELECTED_COURT, selectCourt);

                //we fail the task to skip the next tasks in the workflow and return the existing case details
                context.setTaskFailed(true);
                log.warn("Case ID {} in Awaiting Payment/Awaiting HWF already exists for this user", caseDetails.getCaseId());
            } else if (caseDetails != null) {
                log.trace("Existing Case ID {} found but in {} state", caseDetails.getCaseId(), caseDetails.getState());
            } else {
                log.trace("Existing Case ID not found for user");
            }
        } catch (FeignException e) {
            if (HttpStatus.NOT_FOUND.value() != e.status()) {
                log.error("Unexpected error while checking for duplicate case", e);
                throw e;
            }
        }
        return payload;
    }
}
