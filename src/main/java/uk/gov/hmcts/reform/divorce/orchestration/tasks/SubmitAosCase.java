package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_ANSWER_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DN_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COMPLETED_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class SubmitAosCase implements Task<Map<String, Object>> {

    @Value("${feature-toggle.toggle.feature-toggle-520}")
    private boolean featureToggle520;

    @Autowired
    private CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> submissionData) {
        String authToken = (String) context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String eventId = getAosCompleteEventId(submissionData);

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

    private String getAosCompleteEventId(Map<String, Object> submissionData) {
        if (YES_VALUE.equalsIgnoreCase((String)submissionData.get(RESP_WILL_DEFEND_DIVORCE))) {
            return AWAITING_ANSWER_AOS_EVENT_ID;

        } else if (featureToggle520 && (ADULTERY.equalsIgnoreCase((String)submissionData.get(D_8_REASON_FOR_DIVORCE)))
                && (NO_VALUE.equalsIgnoreCase((String)submissionData.get(RESP_ADMIT_OR_CONSENT_TO_FACT)))) {

            return COMPLETED_AOS_EVENT_ID;
        }

        return AWAITING_DN_AOS_EVENT_ID;
    }
}
