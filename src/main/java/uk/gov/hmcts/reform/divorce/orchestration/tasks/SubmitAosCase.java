package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_ANSWER_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DN_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COMPLETE_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_DEFENDS_DIVORCE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class SubmitAosCase implements Task<Map<String, Object>> {

    @Value("${aos.responded.awaiting-answer.days-to-respond}")
    private int daysToRespond;

    @Autowired
    private CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> submissionData) {
        String authToken = (String) context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String eventId = getAosCompleteEventId(authToken, submissionData);

        Map<String, Object> updateCase = caseMaintenanceClient.updateCase(
            authToken,
            (String) context.getTransientObject(CASE_ID_JSON_KEY),
            eventId,
            submissionData
        );

        if (updateCase != null) {
            updateCase.remove(CCD_CASE_DATA_FIELD);
        }

        return updateCase;
    }

    private String getAosCompleteEventId(String authToken, Map<String, Object> submissionData) {
        if (YES_VALUE.equalsIgnoreCase((String)submissionData.get(RESP_DEFENDS_DIVORCE_CCD_FIELD))) {
            return AWAITING_ANSWER_AOS_EVENT_ID;
        }

        if (YES_VALUE.equalsIgnoreCase((String)submissionData.get(RESP_ADMIT_OR_CONSENT_CCD_FIELD))) {
            return AWAITING_DN_AOS_EVENT_ID;
        }

        CaseDetails caseDetails = caseMaintenanceClient.retrieveAosCase(authToken, true);
        if (UNREASONABLE_BEHAVIOUR.equalsIgnoreCase((String)caseDetails.getCaseData().get(D8_REASON_FOR_DIVORCE))) {
            return AWAITING_DN_AOS_EVENT_ID;
        }

        return COMPLETE_AOS_EVENT_ID;
    }
}
