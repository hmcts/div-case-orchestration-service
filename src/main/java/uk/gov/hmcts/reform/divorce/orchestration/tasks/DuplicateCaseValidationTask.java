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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.ALLOCATED_COURT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.SELECTED_COURT_KEY;

@Component
@Slf4j
public class DuplicateCaseValidationTask implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public DuplicateCaseValidationTask(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        try {
            String transientObject = (String) context.getTransientObject(AUTH_TOKEN_JSON_KEY);
            CaseDetails caseDetails = caseMaintenanceClient.getCase(transientObject);

            if (caseDetails != null && AWAITING_PAYMENT.equalsIgnoreCase(caseDetails.getState())) {
                payload.put(ID, caseDetails.getCaseId());
                payload.put(ALLOCATED_COURT_KEY, caseDetails.getCaseData().get(SELECTED_COURT_KEY));
                //we fail the task to skip the next tasks in the workflow and return the existing case details
                context.setTaskFailed(true);
                log.warn("Case id {} in Awaiting Payment exists for this user", caseDetails.getCaseId());
            }
        } catch (FeignException e) {
            if (HttpStatus.NOT_FOUND.value() != e.status()) {
                throw e;
            }
        }

        return payload;
    }
}
