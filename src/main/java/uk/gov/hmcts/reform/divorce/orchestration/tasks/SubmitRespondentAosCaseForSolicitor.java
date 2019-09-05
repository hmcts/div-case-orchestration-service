package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_2_YR_CONSENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@RequiredArgsConstructor
public class SubmitRespondentAosCaseForSolicitor implements Task<Map<String, Object>> {

    private final CaseMaintenanceClient caseMaintenanceClient;
    private final CcdUtil ccdUtil;
    private String eventId;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> submissionData) {
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String caseID = context.getTransientObject(CASE_ID_JSON_KEY);

        if (isSolicitorRepresentingRespondent(submissionData)) {
            // Maps CCD values of RespAOS2yrConsent & RespAOSAdultery
            // -> RespAdmitOrConsentToFact & RespWillDefendDivorce fields in Case Data
            final String reasonForDivorce = getReasonForDivorce(submissionData);
            final String respAos2yrConsent = (String) submissionData.get(RESP_AOS_2_YR_CONSENT);
            final String respAosAdultery = (String) submissionData.get(RESP_AOS_ADULTERY);

            if ((SEPARATION_2YRS.equalsIgnoreCase(reasonForDivorce)
                    && YES_VALUE.equalsIgnoreCase(respAos2yrConsent))
                || (ADULTERY.equalsIgnoreCase(reasonForDivorce)
                    && YES_VALUE.equalsIgnoreCase(respAosAdultery))) {

                submissionData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
                submissionData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
            }

            if (respondentIsDefending(submissionData)) {
                if (respAdmitsOrConsentsToFact(submissionData)) {
                    eventId = SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID;
                } else {
                    eventId = SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
                }
            } else {
                if (respAdmitsOrConsentsToFact(submissionData)) {
                    eventId = SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID;
                } else {
                    eventId = SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
                }
            }

            submissionData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
            submissionData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());
        }

        Map<String, Object> updateCase = caseMaintenanceClient.updateCase(
                authToken,
                caseID,
                eventId,
                submissionData
        );

        if (updateCase != null) {
            updateCase.remove(CCD_CASE_DATA_FIELD);
        }

        return updateCase;
    }

    private boolean isSolicitorRepresentingRespondent(Map<String, Object> submissionData) {
        final String respondentSolicitorRepresented = (String) submissionData.get(RESP_SOL_REPRESENTED);
        return YES_VALUE.equalsIgnoreCase(respondentSolicitorRepresented);
    }

    private String getReasonForDivorce(Map<String, Object> submissionData) {
        final String reasonForDivorce = (String)submissionData.get(D_8_REASON_FOR_DIVORCE);
        return reasonForDivorce;
    }

    private boolean respAdmitsOrConsentsToFact(Map<String, Object> submissionData) {
        final String respAdmitOrConsentsToFact = (String)submissionData.get(RESP_ADMIT_OR_CONSENT_TO_FACT);
        return YES_VALUE.equalsIgnoreCase(respAdmitOrConsentsToFact);
    }

    private boolean respondentIsDefending(Map<String, Object> submissionData) {
        final String respWillDefendDivorce = (String)submissionData.get(RESP_WILL_DEFEND_DIVORCE);
        return YES_VALUE.equalsIgnoreCase(respWillDefendDivorce);
    }
}
