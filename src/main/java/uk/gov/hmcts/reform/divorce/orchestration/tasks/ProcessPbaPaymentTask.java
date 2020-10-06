package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.PaymentClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeItem;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeValue;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentItem;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.payment.PaymentClientError;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CURRENCY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FIRM_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PBA_PAYMENT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContextHelper.failTask;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.isSolicitorPaymentMethodPba;
import static uk.gov.hmcts.reform.divorce.orchestration.util.payment.PaymentClientError.getCreditAccountPaymentResponse;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessPbaPaymentTask implements Task<Map<String, Object>> {

    private final PaymentClient paymentClient;
    private final AuthTokenGenerator serviceAuthGenerator;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String caseId = getCaseId(context);
        try {
            if (!isSolicitorPaymentMethodPba(caseData)) {
                log.info("Payment option not PBA for CaseID: {}", caseId);
                return caseData;
            }

            log.info("About to make payment for CaseID: {}", caseId);

            processCreditAccountPayment(
                caseId,
                context,
                buildCreditAccountPaymentRequest(context, caseData)
            );

            log.info("Payment made for CaseID: {}", caseId);

        } catch (Exception exception) {
            log.error("Missing required fields for Solicitor Payment on CaseID: {}, with exception {}", caseId, exception.getMessage());
            throw new TaskException(exception);
        }

        return caseData;
    }

    private void processCreditAccountPayment(String caseId,
                                             TaskContext context,
                                             CreditAccountPaymentRequest creditAccountPaymentRequest) {
        try {
            paymentClient.creditAccountPayment(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString(),
                serviceAuthGenerator.generate(),
                creditAccountPaymentRequest
            );
            log.info("Successful payment processed for CaseID: {}", caseId);
        } catch (FeignException exception) {
            log.error("Unsuccessful payment for CaseID: {} with exception: {}", caseId, exception.getMessage());

            failTask(context,
                SOLICITOR_PBA_PAYMENT_ERROR_KEY,
                singletonList(PaymentClientError.getMessage(exception.status(), getCreditAccountPaymentResponse(exception))));
        }
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
        addToRequest(request::setSiteId, caseData.get(DIVORCE_CENTRE_SITEID_JSON_KEY)::toString);
        addToRequest(request::setAccountNumber, caseData.get(SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY)::toString);
        addToRequest(request::setOrganisationName, caseData.get(SOLICITOR_FIRM_JSON_KEY)::toString);
        addToRequest(request::setCustomerReference, caseData.get(SOLICITOR_REFERENCE_JSON_KEY)::toString);
        addToRequest(request::setDescription, value::getFeeDescription);
    }

    private void addToRequest(Consumer<String> setter, Supplier<String> value) {
        Optional.ofNullable(value.get()).ifPresent(setter);
    }
}
