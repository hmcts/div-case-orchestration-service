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

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

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
        // to RespAdmitOrConsentToFact & RespWillDefendDivorce fields in Case Data
        String eventId;
        final TaskContext context = (TaskContext) event.getSource();
        final String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        final String caseID = context.getTransientObject(CASE_ID_JSON_KEY);
        final Map<String, Object> caseData = context.getTransientObject(CCD_CASE_DATA);
        final String reasonForDivorce = (String) caseData.get(D_8_REASON_FOR_DIVORCE);
        final String respAos2yrConsent = (String) caseData.get(RESP_AOS_2_YR_CONSENT);
        final String respAosAdmitAdultery = (String) caseData.get(RESP_AOS_ADMIT_ADULTERY);

        log.info("Attempting to fire secondary AoS Solicitor submission event for case {}", caseID);

        if (respondentIsDefending(caseData)) {
            eventId = SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID;
        } else {
            eventId = SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID;
        }

        if (NO_VALUE.equalsIgnoreCase(respAos2yrConsent) || NO_VALUE.equalsIgnoreCase(respAosAdmitAdultery)) {
            eventId = SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
        }

        log.info("Secondary AoS event to be fired is {} for case {}", eventId, caseID);

        if ((SEPARATION_2YRS.equalsIgnoreCase(reasonForDivorce)
                && YES_VALUE.equalsIgnoreCase(respAos2yrConsent))
            || (ADULTERY.equalsIgnoreCase(reasonForDivorce)
                && YES_VALUE.equalsIgnoreCase(respAosAdmitAdultery))) {

            caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
            caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        }

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

    private boolean respondentIsDefending(Map<String, Object> submissionData) {
        final String respWillDefendDivorce = (String)submissionData.get(RESP_WILL_DEFEND_DIVORCE);
        return YES_VALUE.equalsIgnoreCase(respWillDefendDivorce);
    }
}
