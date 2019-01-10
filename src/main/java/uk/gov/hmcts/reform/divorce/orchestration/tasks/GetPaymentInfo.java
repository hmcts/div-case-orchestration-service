package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.PaymentClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.Payment;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_SESSION_EXISTING_PAYMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PAYMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.INITIATED_PAYMENT_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATUS_FROM_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_PAYMENT_STATUS;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetPaymentInfo implements Task<Map<String, Object>> {

    public static final String PAYMENT_REFERENCE = "PaymentReference";
    public static final String ONLINE = "online";
    public static final String EXTERNAL_REFERENCE = "external_reference";
    public static final String REFERENCE = "reference";
    public static final String DATE_PATTERN = "ddMMyyyy";
    public static final String AMOUNT = "amount";
    public static final String PAYMENT_FEE_ID = "FEE0002";
    private final PaymentClient paymentClient;
    private final AuthTokenGenerator serviceAuthGenerator;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        Object payment = null;
        if (AWAITING_PAYMENT.equalsIgnoreCase((String) context.getTransientObject(CASE_STATE_JSON_KEY))) {
            String caseId = String.valueOf(context.getTransientObject(CASE_ID_JSON_KEY));
            Map<String, Object> successPaymentFromSession = getSuccessPaymentElement(caseData);

            if (successPaymentFromSession != null) {
                String reference = (String) successPaymentFromSession.get(PAYMENT_REFERENCE);
                log.info("Case Id {} has successful payment with ref {}", caseId, reference);
                payment = successPaymentFromSession;
            } else {
                String auth = context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString();
                Optional<Map<String, Object>> successInitiatedPayment = searchSuccessPaymentOnInitiatedState(auth, caseData);

                payment = successInitiatedPayment
                        .map(this::mapPaymentFromPaymentService)
                        .orElse(null);
//                if (successInitiatedPayment != null) {
//                    String reference = (String) successInitiatedPayment.get(PAYMENT_SERVICE_REFERENCE);
//                    log.info("Case Id {} has successful payment on initiated state with ref {}", caseId, reference);
//                    payment = mapPaymentFromPaymentService(successInitiatedPayment);
//
//                }
            }
        }
        Map<String, Object> paymentMap = null;

        if (payment == null) {
            context.setTaskFailed(true);
        } else {
            paymentMap = ImmutableMap.of(
                        PAYMENT, payment,
                        DIVORCE_SESSION_EXISTING_PAYMENTS, caseData.get(D_8_PAYMENTS)
                    );
        }
        return paymentMap;
    }

    private Payment mapPaymentFromPaymentService(Map<String, Object> paymentInfo){
         return Payment.builder()
                .paymentChannel(ONLINE)
                .paymentTransactionId((String) paymentInfo.get(EXTERNAL_REFERENCE))
                .paymentReference((String) paymentInfo.get(REFERENCE))
                .paymentDate(LocalDate.now().toString(DATE_PATTERN))
                .paymentAmount(String.valueOf((Integer)paymentInfo.get(AMOUNT)*100))
                .paymentStatus((String) paymentInfo.get(STATUS_FROM_PAYMENT))
                .paymentFeeId(PAYMENT_FEE_ID)
                .paymentSiteId(getSiteId())
                .build();
    }

    private String getSiteId() {
        return "AA04";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getSuccessPaymentElement(Map<String, Object> divorceCase) {
        return  getStreamFromExistingPayments(divorceCase)
                .map(paymentMapElem -> (Map<String, Object>)paymentMapElem.get(PAYMENT_VALUE))
                .filter(Objects::nonNull)
                .filter(paymentObject -> matchPaymentState(SUCCESS_PAYMENT_STATUS, paymentObject))
                .findFirst()
                .orElse(null);
    }


    @SuppressWarnings("unchecked")
    private Optional<Map<String, Object>> searchSuccessPaymentOnInitiatedState(String auth, Map<String, Object> divorceCase) {
        String serviceToken = serviceAuthGenerator.generate();

        return getStreamFromExistingPayments(divorceCase)
                    .map(paymentMapElem -> (Map<String, Object>)paymentMapElem.get(PAYMENT_VALUE))
                    .map(paymentMapElem -> getSuccessPaymentOnInitiatedState(auth, serviceToken, paymentMapElem))
                    .filter(Objects::nonNull)
                    .findFirst();
    }

    @SuppressWarnings("unchecked")
    private Stream<Map<String, Object>> getStreamFromExistingPayments(Map<String, Object> divorceCase){
        return Optional.ofNullable((List<Map<String, Object>>) divorceCase.get(D_8_PAYMENTS))
                .orElse(Collections.emptyList())
                .stream();
    }

    private Map<String, Object>  getSuccessPaymentOnInitiatedState(
            String auth,
            String serviceToken,
            Map<String, Object> paymentObject) {

        if (paymentObject != null && INITIATED_PAYMENT_STATUS.equalsIgnoreCase(String.valueOf(paymentObject.get(PAYMENT_STATUS)))) {
            Map<String, Object> payment = paymentClient.checkPayment(auth,
                    serviceToken,
                    (String) paymentObject.get(OrchestrationConstants.PAYMENT_REFERENCE));
            if (SUCCESS_PAYMENT_STATUS.equalsIgnoreCase(String.valueOf(payment.get(STATUS_FROM_PAYMENT)))) {
                return payment;
            }
        }
        return null;
    }

    private boolean matchPaymentState(String expectedStatus, Map<String, Object> paymentObject) {
        return expectedStatus.equalsIgnoreCase((String) paymentObject.get(PAYMENT_STATUS));
    }
}