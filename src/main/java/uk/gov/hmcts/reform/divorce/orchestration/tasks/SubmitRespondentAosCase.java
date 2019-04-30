package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_ANSWER_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DN_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COMPLETED_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class SubmitRespondentAosCase implements Task<Map<String, Object>> {

    private static final String RESP_CORRESPONDENCE_SEND_TO_SOL = "D8RespondentCorrespondenceSendToSol";
    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public SubmitRespondentAosCase(final CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> submissionData) {
        String authToken = (String) context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String caseIDJsonKey = (String) context.getTransientObject(CASE_ID_JSON_KEY);

        String eventId;

        if(isSolicitorRepresentingRespondent(submissionData)) {
            //move back to AOS awaiting, as technically the nominated solicitor will provide a response
            eventId = AOS_AWAITING;
        } else {
            //if respondent didn't nominate a solicitor, then they've provided an answer
            if (isRespondentWillDefendDivorce(submissionData)) {
                eventId = AWAITING_ANSWER_AOS_EVENT_ID;
            } else if (isRespondentAgreeingDivorceButNotAdmittingFact(submissionData, context)) {
                eventId = COMPLETED_AOS_EVENT_ID;
            } else {
                eventId = AWAITING_DN_AOS_EVENT_ID;
            }
            submissionData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
            submissionData.put(RECEIVED_AOS_FROM_RESP_DATE, CcdUtil.getCurrentDate());
        }

        Map<String, Object> updateCase = caseMaintenanceClient.updateCase(
            authToken,
            caseIDJsonKey,
            eventId,
            submissionData
        );

        if (updateCase != null) {
            updateCase.remove(CCD_CASE_DATA_FIELD);
        }

        return updateCase;
    }

    private boolean isSolicitorRepresentingRespondent(Map<String, Object> submissionData) {
        final String respondentCorrespondenceSendToSolicitor = (String) submissionData.get(RESP_CORRESPONDENCE_SEND_TO_SOL);
        return YES_VALUE.equalsIgnoreCase(respondentCorrespondenceSendToSolicitor);
    }

    private boolean isRespondentWillDefendDivorce(Map<String, Object> submissionData) {
        final String respWillDefendDivorce = (String)submissionData.get(RESP_WILL_DEFEND_DIVORCE);
        return YES_VALUE.equalsIgnoreCase(respWillDefendDivorce);
    }

    private boolean isRespondentAgreeingDivorceButNotAdmittingFact(Map<String, Object> submissionData, TaskContext context) {
        final String respAdmitOrConsentToFact = (String)submissionData.get(RESP_ADMIT_OR_CONSENT_TO_FACT);
        final CaseDetails currentCaseDetails = caseMaintenanceClient.retrievePetitionById(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString(),
                context.getTransientObject(CASE_ID_JSON_KEY).toString()
        );

        final String reasonForDivorce = (String)currentCaseDetails.getCaseData().get(D_8_REASON_FOR_DIVORCE);
        return (ADULTERY.equalsIgnoreCase(reasonForDivorce)
                || SEPARATION_2YRS.equalsIgnoreCase(reasonForDivorce))
                && NO_VALUE.equalsIgnoreCase(respAdmitOrConsentToFact);
    }
}
