package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.config.EventConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType.aosReceivedNoAdConStarted;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType.aosSubmittedDefended;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType.aosSubmittedUndefended;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_NOMINATE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;

@Component
@RequiredArgsConstructor
public class SubmitRespondentAosCase implements Task<Map<String, Object>> {

    private static final String RESP_SOL_REPRESENTED = "respondentSolicitorRepresented";
    private final CaseMaintenanceClient caseMaintenanceClient;
    private final CcdUtil ccdUtil;
    private final EventConfig eventConfig;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> submissionData) {
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String caseIDJsonKey = context.getTransientObject(CASE_ID_JSON_KEY);

        String eventId;
        if (isSolicitorRepresentingRespondent(submissionData)) {
            //move back to AOS awaiting, as technically the nominated solicitor will provide a response
            eventId = AOS_NOMINATE_SOLICITOR;
        } else {
            //if respondent didn't nominate a solicitor, then they've provided an answer
            if (isRespondentDefendingDivorce(submissionData)) {
                eventId = getEventName(context, aosSubmittedDefended.getEventId());
            } else if (isRespondentAgreeingDivorceButNotAdmittingFact(submissionData, context)) {
                eventId = getEventName(context, aosReceivedNoAdConStarted.getEventId());
            } else {
                eventId = getEventName(context, aosSubmittedUndefended.getEventId());
            }
            submissionData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
            submissionData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());
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

    private String getEventName(final TaskContext context, final String currentEvent) {
        CaseDetails currentCaseDetails =
                Optional.ofNullable(context.<CaseDetails>getTransientObject(CASE_DETAILS_JSON_KEY)).orElse(
                caseMaintenanceClient.retrievePetitionById(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString(),
                context.getTransientObject(CASE_ID_JSON_KEY).toString()));

        Optional<LanguagePreference> languagePreference = CaseDataUtils.getLanguagePreference(currentCaseDetails.getCaseData());
        return Optional.ofNullable(eventConfig.getEvents()
                .get(languagePreference.orElse(LanguagePreference.ENGLISH))
                .get(EventType.getEvenType(currentEvent))).orElse(currentEvent);
    }

    private boolean isSolicitorRepresentingRespondent(Map<String, Object> submissionData) {
        final String respondentSolicitorRepresented = (String) submissionData.get(RESP_SOL_REPRESENTED);
        return YES_VALUE.equalsIgnoreCase(respondentSolicitorRepresented);
    }

    private boolean isRespondentDefendingDivorce(Map<String, Object> submissionData) {
        final String respWillDefendDivorce = (String) submissionData.get(RESP_WILL_DEFEND_DIVORCE);
        return YES_VALUE.equalsIgnoreCase(respWillDefendDivorce);
    }

    private boolean isRespondentAgreeingDivorceButNotAdmittingFact(Map<String, Object> submissionData, TaskContext context) {
        final String respAdmitOrConsentToFact = (String) submissionData.get(RESP_ADMIT_OR_CONSENT_TO_FACT);
        final CaseDetails currentCaseDetails = caseMaintenanceClient.retrievePetitionById(
            context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString(),
            context.getTransientObject(CASE_ID_JSON_KEY).toString()
        );

        final String reasonForDivorce = (String) currentCaseDetails.getCaseData().get(D_8_REASON_FOR_DIVORCE);
        return (ADULTERY.getValue().equalsIgnoreCase(reasonForDivorce)
            || SEPARATION_TWO_YEARS.getValue().equalsIgnoreCase(reasonForDivorce))
            && NO_VALUE.equalsIgnoreCase(respAdmitOrConsentToFact);
    }
}
