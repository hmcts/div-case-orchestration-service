package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Service
@RequiredArgsConstructor
public class SolicitorAosEventServiceImpl implements SolicitorAosEventService {

    private final CaseMaintenanceClient caseMaintenanceClient;
    private final CcdUtil ccdUtil;

    @Override
    @EventListener
    public Map<String, Object> fireSecondaryAosEvent(SubmitSolicitorAosEvent event) {

        // Maps CCD values of RespAOS2yrConsent & RespAOSAdultery
        // -> RespAdmitOrConsentToFact & RespWillDefendDivorce fields in Case Data
        String eventId;
        TaskContext context = (TaskContext) event.getSource();
        Map<String, Object> caseData = context.getTransientObject(CCD_CASE_DATA);
        final String reasonForDivorce = getReasonForDivorce(caseData);
        final String respAos2yrConsent = (String) caseData.get(RESP_AOS_2_YR_CONSENT);
        final String respAosAdultery = (String) caseData.get(RESP_AOS_ADULTERY);

        if (respondentIsDefending(caseData)) {
            eventId = SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID;
        } else {
            eventId = SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID;
        }

        if (NO_VALUE.equalsIgnoreCase(respAos2yrConsent) || NO_VALUE.equalsIgnoreCase(respAosAdultery)) {
            eventId = SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
        }

        if ((SEPARATION_2YRS.equalsIgnoreCase(reasonForDivorce)
                && YES_VALUE.equalsIgnoreCase(respAos2yrConsent))
            || (ADULTERY.equalsIgnoreCase(reasonForDivorce)
                && YES_VALUE.equalsIgnoreCase(respAosAdultery))) {

            caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
            caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        }

        caseData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        caseData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String caseID = context.getTransientObject(CASE_ID_JSON_KEY);

        caseMaintenanceClient.updateCase(
            authToken,
            caseID,
            eventId,
            caseData
        );

        return caseData;
    }

    private String getReasonForDivorce(Map<String, Object> submissionData) {
        final String reasonForDivorce = (String)submissionData.get(D_8_REASON_FOR_DIVORCE);
        return reasonForDivorce;
    }

    private boolean respondentIsDefending(Map<String, Object> submissionData) {
        final String respWillDefendDivorce = (String)submissionData.get(RESP_WILL_DEFEND_DIVORCE);
        return YES_VALUE.equalsIgnoreCase(respWillDefendDivorce);
    }
}
