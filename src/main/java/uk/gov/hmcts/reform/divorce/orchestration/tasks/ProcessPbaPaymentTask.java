package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.PaymentClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeItem;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeValue;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentItem;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentStatus;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CURRENCY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PBA_PAYMENT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
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
    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;
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
                buildCreditAccountPaymentRequest(context, caseData)
            );
        } catch (FeignException exception) {
            log.error("CaseID: {} Unsuccessful payment for account number {} with exception {}", caseId, pbaNumber, exception.getMessage());

            failTask(context,
                SOLICITOR_PBA_PAYMENT_ERROR_KEY,
                singletonList(getMessage(pbaNumber, exception)));
        }
        return paymentResponseResponseEntity;
    }

    private CreditAccountPaymentRequest buildCreditAccountPaymentRequest(TaskContext context, Map<String, Object> caseData) {
        CreditAccountPaymentRequest creditAccountPaymentRequest = new CreditAccountPaymentRequest();
        OrderSummary orderSummary = getOrderSummary(caseData);

        creditAccountPaymentRequest.setService(SERVICE);
        creditAccountPaymentRequest.setCurrency(CURRENCY);

        final FeeValue feeValue = getFeeValue(orderSummary);
        populatePaymentRequest(
            context,
            caseData,
            creditAccountPaymentRequest,
            orderSummary,
            feeValue);

        List<PaymentItem> paymentItemList = populateFeesPaymentItems(context, caseData, orderSummary, feeValue);
        creditAccountPaymentRequest.setFees(paymentItemList);

        return creditAccountPaymentRequest;
    }

    private OrderSummary getOrderSummary(Map<String, Object> caseData) {
        return objectMapper.convertValue(
            caseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY),
            OrderSummary.class);
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

    private List<PaymentItem> populateFeesPaymentItems(TaskContext context, Map<String, Object> caseData, OrderSummary orderSummary, FeeValue value) {
        PaymentItem paymentItem = new PaymentItem();
        addToRequest(paymentItem::setCcdCaseNumber, context.getTransientObject(CASE_ID_JSON_KEY)::toString);
        addToRequest(paymentItem::setCalculatedAmount, orderSummary::getPaymentTotal);
        addToRequest(paymentItem::setCode, value::getFeeCode);
        addToRequest(paymentItem::setReference, caseData.get(SOLICITOR_REFERENCE_JSON_KEY)::toString);
        addToRequest(paymentItem::setVersion, value::getFeeVersion);
        return singletonList(paymentItem);
    }

    private FeeValue getFeeValue(OrderSummary orderSummary) {
        // We are always interested in the first fee. There may be a change in the future
        FeeItem feeItem = orderSummary.getFees().get(0);
        return feeItem.getValue();
    }

    private void populatePaymentRequest(TaskContext context, Map<String, Object> caseData, CreditAccountPaymentRequest request,
                                        OrderSummary orderSummary, FeeValue value) {
        addToRequest(request::setAmount, orderSummary::getPaymentTotal);
        addToRequest(request::setCcdCaseNumber, context.getTransientObject(CASE_ID_JSON_KEY)::toString);

        if (featureToggleService.isFeatureEnabled(Features.PBA_USING_CASE_TYPE)) {
            addToRequest(request::setCaseType, caseData.get(CASE_TYPE_ID)::toString);
        } else {
            addToRequest(request::setSiteId, caseData.get(DIVORCE_CENTRE_SITEID_JSON_KEY)::toString);
        }
        addToRequest(request::setAccountNumber, getPbaNumber(caseData, isPbaToggleOn())::toString);
        addToRequest(request::setOrganisationName, caseData.get(PETITIONER_SOLICITOR_FIRM)::toString);
        addToRequest(request::setCustomerReference, caseData.get(SOLICITOR_REFERENCE_JSON_KEY)::toString);
        addToRequest(request::setDescription, value::getFeeDescription);
    }

    private void addToRequest(Consumer<String> setter, Supplier<String> value) {
        Optional.ofNullable(value.get()).ifPresent(setter);
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
