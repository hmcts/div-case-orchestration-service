package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.PaymentClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentStatus;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PBA_PAYMENT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContextHelper.failTask;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.getPbaNumber;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getAuthToken;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.isSolicitorPaymentMethodPba;
import static uk.gov.hmcts.reform.divorce.orchestration.util.payment.PbaClientError.getMessage;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessPbaPaymentTask implements Task<Map<String, Object>> {

    private final PaymentClient paymentClient;
    private final AuthTokenGenerator serviceAuthGenerator;
    private final FeatureToggleService featureToggleService;
    private final CreditAccountPaymentRequestBuilder creditAccountPaymentRequestBuilder;
    public static final String PAYMENT_STATUS = "PaymentStatus";
    public static final String DEFAULT_END_STATE_FOR_NON_PBA_PAYMENTS = CcdStates.SOLICITOR_AWAITING_PAYMENT_CONFIRMATION;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String caseId = getCaseId(context);
        try {
            if (!isSolicitorPaymentMethodPba(caseData)) {
                log.info("CaseID: {} Payment option not PBA", caseId);
                return caseData;
            }

            log.info("CaseID: {} About to make payment", caseId);

            ResponseEntity<CreditAccountPaymentResponse> paymentResponseEntity = processCreditAccountPayment(
                caseId,
                context,
                caseData
            );

            String paymentStatus = getPaymentStatus(paymentResponseEntity);

            if (isPaymentStatusSuccess(paymentStatus)) {
                addPaymentStatusToResponse(caseData, paymentStatus);
            }

            log.info("CaseID: {} Solicitor Credit account payment completed with payment status: {}", caseId, paymentStatus);
        } catch (Exception exception) {
            log.error("CaseID: {} Missing required fields for Solicitor Payment with exception {}", caseId, exception.getMessage());
            throw new TaskException(exception);
        }

        return caseData;
    }

    private ResponseEntity<CreditAccountPaymentResponse> processCreditAccountPayment(String caseId,
                                                                                     TaskContext context,
                                                                                     Map<String, Object> caseData) {
        ResponseEntity<CreditAccountPaymentResponse> paymentResponseResponseEntity = null;
        String pbaNumber = getPbaNumber(caseData, isPbaToggleOn());
        try {
            paymentResponseResponseEntity = paymentClient.creditAccountPayment(
                getAuthToken(context),
                serviceAuthGenerator.generate(),
                creditAccountPaymentRequestBuilder.buildCreditAccountPaymentRequest(context, caseData)
            );
        } catch (FeignException exception) {
            log.error("CaseID: {} Unsuccessful payment for account number {} with exception {}", caseId, pbaNumber, exception.getMessage());

            failTask(context,
                SOLICITOR_PBA_PAYMENT_ERROR_KEY,
                singletonList(getMessage(pbaNumber, exception)));
        }
        return paymentResponseResponseEntity;
    }

    private String getPaymentStatus(ResponseEntity<CreditAccountPaymentResponse> paymentResponseEntity) {
        return Optional.ofNullable(paymentResponseEntity)
            .map(response ->
                Optional.ofNullable(response.getBody())
                    .map(CreditAccountPaymentResponse::getStatus)
                    .orElseGet(() -> EMPTY)
            )
            .orElseGet(() -> EMPTY);
    }

    private void addPaymentStatusToResponse(Map<String, Object> caseData, String paymentStatus) {
        caseData.put(PAYMENT_STATUS, paymentStatus);
    }

    private boolean isPaymentStatusSuccess(String paymentStatus) {
        return PaymentStatus.SUCCESS.value().equalsIgnoreCase(paymentStatus);
    }

    private boolean isPbaToggleOn() {
        return featureToggleService.isFeatureEnabled(Features.PAY_BY_ACCOUNT);
    }

}
