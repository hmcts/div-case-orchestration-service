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

import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.isSolicitorPaymentMethodPba;

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

    public static String getPbaUpdatedState(CcdCallbackRequest ccdCallbackRequest, Map<String, Object> caseData) {
        String currentState = ccdCallbackRequest.getCaseDetails().getState();
        String paymentStatus = (String) caseData.get(ProcessPbaPaymentTask.PAYMENT_STATUS);
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        log.info("CaseID: {} Payment status is '{}', current case state is '{}'", caseId, paymentStatus, currentState);

        if (isPbaCaseStateToBeUpdated(caseData, paymentStatus)) {
            currentState = CcdStates.SUBMITTED;
            log.info("CaseID: {} Updated case state to '{}'", currentState, caseId);
        } else {
            log.info("CaseID: {} State not updated. Current state is '{}'", caseId, currentState);
        }

        log.info("CaseID: {} Removing temporary payment status property '{}' in case data", caseId, ProcessPbaPaymentTask.PAYMENT_STATUS);
        caseData.remove(ProcessPbaPaymentTask.PAYMENT_STATUS);

        return currentState;
    }

    public static boolean isResponseErrors(String errorKey, Map<String, Object> errorResponse) {
        return Optional.ofNullable(errorResponse)
            .map(errors ->
                Optional.ofNullable(errorKey)
                    .map(errors::containsKey)
                    .orElseGet(() -> false)
            )
            .orElseGet(() -> false);
    }

    public static List<String> getResponseErrors(String errorKey, Map<String, Object> errorResponse) {
        return Optional.ofNullable(errorResponse)
            .map(errors -> (List<String>) errors.get(errorKey))
            .orElseGet(() -> null);
    }

    // Returns true if payment status is success and is fee account payment
    private static boolean isPbaCaseStateToBeUpdated(Map<String, Object> caseData, String paymentStatus) {
        return isSolicitorPaymentMethodPba(caseData)
            && PaymentStatus.SUCCESS.value().equalsIgnoreCase(paymentStatus);
    }

}