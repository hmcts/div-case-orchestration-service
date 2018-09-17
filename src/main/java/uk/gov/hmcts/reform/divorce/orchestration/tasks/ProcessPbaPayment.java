package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CURRENCY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FIRM_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;

@Component
@Slf4j
public class ProcessPbaPayment implements Task<Map<String, Object>> {

    private final PaymentClient paymentClient;
    private final AuthTokenGenerator serviceAuthGenerator;
    private final ObjectMapper objectMapper;

    @Autowired
    public ProcessPbaPayment(PaymentClient paymentClient,
                             AuthTokenGenerator serviceAuthGenerator,
                             ObjectMapper objectMapper) {
        this.paymentClient = paymentClient;
        this.serviceAuthGenerator = serviceAuthGenerator;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        try {
            if (solicitorPayByAccount((String) caseData.get(SOLICITOR_HOW_TO_PAY_JSON_KEY))) {
                CreditAccountPaymentRequest request = new CreditAccountPaymentRequest();
                OrderSummary orderSummary = objectMapper.convertValue(
                        caseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY),
                        OrderSummary.class);

                request.setService(SERVICE);
                request.setCurrency(CURRENCY);

                // We are always interested in the first fee. There may be a change in the future
                FeeItem feeItem = orderSummary.getFees().get(0);
                final FeeValue value = feeItem.getValue();
                addToRequest(request::setAmount, orderSummary::getPaymentTotal);
                addToRequest(request::setCcdCaseNumber, context.getTransientObject(CASE_ID_JSON_KEY)::toString);
                addToRequest(request::setSiteId, caseData.get(DIVORCE_CENTRE_SITEID_JSON_KEY)::toString);
                addToRequest(request::setAccountNumber, caseData.get(SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY)::toString);
                addToRequest(request::setOrganisationName, caseData.get(SOLICITOR_FIRM_JSON_KEY)::toString);
                addToRequest(request::setCustomerReference, caseData.get(SOLICITOR_REFERENCE_JSON_KEY)::toString);
                addToRequest(request::setDescription, value::getFeeDescription);

                //populate feesItem
                PaymentItem paymentItem = new PaymentItem();
                addToRequest(paymentItem::setCcdCaseNumber, context.getTransientObject(CASE_ID_JSON_KEY)::toString);
                addToRequest(paymentItem::setCalculatedAmount, orderSummary::getPaymentTotal);
                addToRequest(paymentItem::setCode, value::getFeeCode);
                addToRequest(paymentItem::setReference, caseData.get(SOLICITOR_REFERENCE_JSON_KEY)::toString);
                addToRequest(paymentItem::setVersion, value::getFeeVersion);
                List<PaymentItem> paymentItemList = Collections.singletonList(paymentItem);
                request.setFees(paymentItemList);

                log.info("About to make payment on case Id - "
                        + context.getTransientObject(CASE_ID_JSON_KEY));

                paymentClient.creditAccountPayment(
                        context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString(),
                        serviceAuthGenerator.generate(),
                        request
                );

                log.info("Successful payment processed on case Id - "
                        + context.getTransientObject(CASE_ID_JSON_KEY));
            }
        } catch (NullPointerException exception) {
            log.error("Missing required fields for Solicitor Payment on case Id - "
                + context.getTransientObject(CASE_ID_JSON_KEY), exception);
            throw new TaskException(exception.getMessage());
        }

        return caseData;
    }

    private void addToRequest(Consumer<String> setter, Supplier<String> value) {
        Optional.ofNullable(value.get()).ifPresent(setter);
    }

    private boolean solicitorPayByAccount(String howPay) {
        return Optional.ofNullable(howPay)
                .map(i -> i.equals(FEE_PAY_BY_ACCOUNT))
                .orElse(false);
    }
}
