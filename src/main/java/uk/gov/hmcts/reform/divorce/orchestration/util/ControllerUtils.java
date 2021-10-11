package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentStatus;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ProcessPbaPaymentTask;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.BO_WELSH_AOS_RECEIVED_NO_AD_CON_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.COMPLETED_AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_PRE_SUBMITTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_2_YR_CONSENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_ADMIT_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ControllerUtils {

    public static ResponseEntity<CcdCallbackResponse> responseWithData(Map<String, Object> data) {
        return ResponseEntity.ok(ccdResponseWithData(data));
    }

    public static ResponseEntity<CcdCallbackResponse> responseWithErrors(List<String> errors) {
        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .errors(errors)
            .build());
    }

    public static CcdCallbackRequest ccdRequestWithData(Map<String, Object> caseData) {
        return CcdCallbackRequest.builder()
            .caseDetails(caseDetailsWithData(caseData))
            .build();
    }

    public static CcdCallbackResponse ccdResponseWithData(Map<String, Object> caseData) {
        return CcdCallbackResponse.builder()
            .data(caseData)
            .build();
    }

    public static CaseDetails caseDetailsWithData(Map<String, Object> caseData) {
        return CaseDetails.builder()
            .caseData(caseData)
            .build();
    }

    public static String getPbaUpdatedState(String caseId, Map<String, Object> caseData) {

        log.info("CaseID: {} Removing temporary payment status property '{}' in case data", caseId, ProcessPbaPaymentTask.PAYMENT_STATUS);
        caseData.remove(ProcessPbaPaymentTask.PAYMENT_STATUS);

        log.info("CaseID: {} Updating case state to '{}'", CcdStates.SUBMITTED, caseId);
        return CcdStates.SUBMITTED;
    }

    public static boolean hasErrorKeyInResponse(String errorKey, Map<String, Object> response) {
        return Optional.ofNullable(response)
            .map(responseMap -> responseMap.containsKey(errorKey))
            .orElse(false);
    }

    public static List<String> getResponseErrors(String errorKey, Map<String, Object> errorResponse) {
        return Optional.ofNullable(errorResponse)
            .map(errors -> (List<String>) errors.get(errorKey))
            .orElseGet(() -> null);
    }

    public static boolean isPaymentSuccess(Map<String, Object> caseData) {
        return Optional.ofNullable((String) caseData.get(ProcessPbaPaymentTask.PAYMENT_STATUS))
            .map(i -> i.equalsIgnoreCase(PaymentStatus.SUCCESS.value()))
            .orElse(false);
    }

    public static String stateForAosReceivedNoAdCon(Map<String, Object> caseData) {
        if (isRespondentRepresented(caseData)) {
            return AOS_PRE_SUBMITTED;
        }

        final String respAos2yrConsent = (String) caseData.get(RESP_AOS_2_YR_CONSENT);
        final String respAosAdmitAdultery = (String) caseData.get(RESP_AOS_ADMIT_ADULTERY);
        if (NO_VALUE.equalsIgnoreCase(respAos2yrConsent) || NO_VALUE.equalsIgnoreCase(respAosAdmitAdultery)) {
            return AOS_COMPLETED;
        }

        final String respWillDefendDivorce = (String) caseData.get(RESP_WILL_DEFEND_DIVORCE);
        if (YES_VALUE.equalsIgnoreCase(respWillDefendDivorce)) {
            return AOS_SUBMITTED_AWAITING_ANSWER;
        }

        return AWAITING_DECREE_NISI;
    }

    public static boolean isAosReceivedNoAdCon(String eventId) {
        if (BO_WELSH_AOS_RECEIVED_NO_AD_CON_STARTED.equals(eventId) || COMPLETED_AOS.equals(eventId)) {
            return true;
        }

        return false;
    }
}