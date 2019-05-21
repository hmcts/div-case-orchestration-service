package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_RECEIVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_RECEIVED_AOS_COMPLETE;

@Component
public class SubmitDnCase implements Task<Map<String, Object>> {

    @Autowired
    private CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public SubmitDnCase(final CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String caseId = context.getTransientObject(CASE_ID_JSON_KEY);

        final CaseDetails currentCaseDetails = caseMaintenanceClient.retrievePetitionById(
                authToken,
                caseId
        );

        String eventId = getDnEventId(currentCaseDetails);

        Map<String, Object> updateCase = caseMaintenanceClient.updateCase(
            authToken,
            caseId,
            eventId,
            caseData
        );

        if (updateCase != null) {
            updateCase.remove(CCD_CASE_DATA_FIELD);
        }

        return updateCase;
    }

    private String getDnEventId(final CaseDetails currentCaseDetails) {

        final String caseState = currentCaseDetails.getState();

        if (AOS_COMPLETED.equalsIgnoreCase(caseState)) {
            return DN_RECEIVED_AOS_COMPLETE;

        } else {

            return DN_RECEIVED;
        }
    }
}
