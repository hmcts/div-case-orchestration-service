package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.SubmitSolicitorAosEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.SolicitorAosEventService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_2_YR_CONSENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_ADMIT_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UI_ONLY_RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_FIVE_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitorAosEventServiceImpl implements SolicitorAosEventService {

    private final CaseMaintenanceClient caseMaintenanceClient;
    private final CcdUtil ccdUtil;

    @Override
    @EventListener
    public Map<String, Object> fireSecondaryAosEvent(SubmitSolicitorAosEvent event) {
        // Maps CCD values of RespAOS2yrConsent & RespAOSAdultery
        // to RespAdmitOrConsentToFact & RespWillDefendDivorce fields in Case Data.
        // Due to limitation on CCD UI, UI_ONLY_RESP_WILL_DEFEND_DIVORCE is used for
        // 2yr separation, 5yr separation and adultery to match the journey requirements
        String eventId;
        final TaskContext context = (TaskContext) event.getSource();
        final String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        final String caseID = context.getTransientObject(CASE_ID_JSON_KEY);
        final Map<String, Object> caseData = context.getTransientObject(CCD_CASE_DATA);
        final String reasonForDivorce = (String) caseData.get(D_8_REASON_FOR_DIVORCE);
        final String respAos2yrConsent = (String) caseData.get(RESP_AOS_2_YR_CONSENT);
        final String respAosAdmitAdultery = (String) caseData.get(RESP_AOS_ADMIT_ADULTERY);

        if (SEPARATION_TWO_YEARS.equalsIgnoreCase(reasonForDivorce) || ADULTERY.equalsIgnoreCase(reasonForDivorce)) {
            if (YES_VALUE.equalsIgnoreCase(respAos2yrConsent) || YES_VALUE.equalsIgnoreCase(respAosAdmitAdultery)) {
                // for 2yr separation and adultery, if respondent admits fact, assume not defended
                caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
                caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
            } else {
                // if respondent does not admit fact, take secondary UI_ONLY_RESP_WILL_DEFEND_DIVORCE
                // value and map directly onto existing RESP_WILL_DEFEND_DIVORCE
                caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
                caseData.put(RESP_WILL_DEFEND_DIVORCE, caseData.get(UI_ONLY_RESP_WILL_DEFEND_DIVORCE));
            }
        }

        if (SEPARATION_FIVE_YEARS.equalsIgnoreCase(reasonForDivorce)) {
            // for 5 yr separation, no consent is asked, we just map over
            // UI_ONLY_RESP_WILL_DEFEND_DIVORCE to RESP_WILL_DEFEND_DIVORCE
            caseData.put(RESP_WILL_DEFEND_DIVORCE, caseData.get(UI_ONLY_RESP_WILL_DEFEND_DIVORCE));
        }

        eventId = getEventId(caseData);

        log.info("Secondary AoS event to be fired is {} for case {}", eventId, caseID);

        caseData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        caseData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        caseMaintenanceClient.updateCase(
            authToken,
            caseID,
            eventId,
            caseData
        );

        return caseData;
    }

    private String getEventId(Map<String, Object> caseData) {
        final String respAos2yrConsent = (String) caseData.get(RESP_AOS_2_YR_CONSENT);
        final String respAosAdmitAdultery = (String) caseData.get(RESP_AOS_ADMIT_ADULTERY);
        String eventId;
        if (respondentIsDefending(caseData)) {
            eventId = SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID;
        } else {
            eventId = SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID;
        }

        if (NO_VALUE.equalsIgnoreCase(respAos2yrConsent) || NO_VALUE.equalsIgnoreCase(respAosAdmitAdultery)) {
            eventId = SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
        }
        return eventId;
    }

    private boolean respondentIsDefending(Map<String, Object> submissionData) {
        // as we have already mapped over UI_ONLY_RESP_WILL_DEFEND_DIVORCE to RESP_WILL_DEFEND_DIVORCE
        // we only need to check the main property here
        final String respWillDefendDivorce = (String) submissionData.get(RESP_WILL_DEFEND_DIVORCE);
        return YES_VALUE.equalsIgnoreCase(respWillDefendDivorce);
    }
}
