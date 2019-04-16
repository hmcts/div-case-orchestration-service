package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_HWF_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow.SELECTED_COURT;

@Component
@Slf4j
public class DuplicateCaseValidationTask implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;
    private Boolean receivedAosFromResp = false;
    private Boolean receivedAosFromCoResp = false;

    @Autowired
    public DuplicateCaseValidationTask(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        try {
            String transientObject = (String) context.getTransientObject(AUTH_TOKEN_JSON_KEY);
            CaseDetails caseDetails = caseMaintenanceClient.getCase(transientObject);

            if (caseDetails != null) {
                Map<String, Object> caseData = caseDetails.getCaseData();
                String d8DivorceUnit = (String) caseData.get(D_8_DIVORCE_UNIT);
                String caseState = caseDetails.getState();
                getReceivedAnswers(caseData);

                if (AWAITING_PAYMENT.equalsIgnoreCase(caseState)
                        || AWAITING_HWF_DECISION.equalsIgnoreCase(caseState)
                        || receivedAosFromResp
                        || receivedAosFromCoResp) {
                    // DO THE SAME HERE FOR DN ANSWER?
                    payload.put(ID, caseDetails.getCaseId());
                    context.setTransientObject(SELECTED_COURT, d8DivorceUnit);

                    //we fail the task to skip the next tasks in the workflow and return the existing case details
                    context.setTaskFailed(true);
                    log.warn("User already has existing case - Case ID {}", caseDetails.getCaseId());
                } else {
                    log.trace("Existing Case ID not found for user");
                }
            }
        } catch (FeignException e) {
            if (HttpStatus.NOT_FOUND.value() != e.status()) {
                log.error("Unexpected error while checking for duplicate case", e);
                throw e;
            }
        }
        return payload;
    }

    private void getReceivedAnswers(Map<String, Object> caseData) {
        if (caseData.get(RECEIVED_AOS_FROM_RESP) != null
                && ((String) caseData.get(RECEIVED_AOS_FROM_RESP)).equalsIgnoreCase(YES_VALUE)) {
            receivedAosFromResp = true;
        }

        if (caseData.get(RECEIVED_AOS_FROM_CO_RESP) != null
                && ((String) caseData.get(RECEIVED_AOS_FROM_CO_RESP)).equalsIgnoreCase(YES_VALUE)) {
            receivedAosFromCoResp = true;
        }
    }
}
