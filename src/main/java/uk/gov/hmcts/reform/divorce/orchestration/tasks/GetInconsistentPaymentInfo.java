package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.PaymentClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PAYMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EXTERNAL_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.INITIATED_PAYMENT_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_AMOUNT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_CHANNEL_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_DATE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_FEE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_FEE_ID_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_SERVICE_AMOUNT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_SERVICE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_SITE_ID_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_STATUS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_TRANSACTION_ID_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATUS_FROM_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_PAYMENT_STATUS;


@Component
@RequiredArgsConstructor
@Slf4j
public class GetInconsistentPaymentInfo implements Task<Map<String, Object>> {

    private final PaymentClient paymentClient;
    private final AuthTokenGenerator serviceAuthGenerator;
    private final TaskCommons taskCommons;
    private final CcdUtil ccdUtil;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        Object payment = null;
        if (AWAITING_PAYMENT.equalsIgnoreCase(context.getTransientObject(CASE_STATE_JSON_KEY))) {
            Map<String, Object> successPaymentFromSession = searchSuccessStatePayment(caseData);

            if (successPaymentFromSession != null) {
                payment = mapPaymentFromCCDModel(successPaymentFromSession);
            } else {
                String auth = context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString();
                Optional<Map<String, Object>> successInitiatedPayment =
                    searchSuccessPaymentOnInitiatedState(auth, caseData);
                final String siteId = getSiteId(caseData);
                payment = successInitiatedPayment
                    .map(paymentInfo -> mapPaymentFromPaymentService(paymentInfo, siteId))
                    .orElse(null);
            }
        }
        Map<String, Object> paymentMap = null;

        if (payment == null) {
            context.setTaskFailed(true);
        } else {
            String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
            String reference = (String) ((Map) payment).get(PAYMENT_REFERENCE);
            log.info("Case Id {} has successful payment with ref {}", caseId, reference);
            paymentMap = ImmutableMap.of(
                PAYMENT, payment,
                DIVORCE_SESSION_EXISTING_PAYMENTS, caseData.get(D_8_PAYMENTS)
            );
        }
        return paymentMap;
    }

    private Map<String, Object> mapPaymentFromPaymentService(Map<String, Object> paymentInfo, String siteId) {
        return new ImmutableMap.Builder<String, Object>()
            .put(PAYMENT_CHANNEL_KEY, PAYMENT_CHANNEL)
            .put(PAYMENT_TRANSACTION_ID_KEY, paymentInfo.get(EXTERNAL_REFERENCE))
            .put(PAYMENT_REFERENCE_KEY, paymentInfo.get(PAYMENT_SERVICE_REFERENCE))
            .put(PAYMENT_DATE_KEY, ccdUtil.getCurrentDatePaymentFormat())
            .put(PAYMENT_AMOUNT_KEY, String.valueOf((Integer) paymentInfo.get(PAYMENT_SERVICE_AMOUNT_KEY) * 100))
            .put(PAYMENT_STATUS_KEY, paymentInfo.get(STATUS_FROM_PAYMENT))
            .put(PAYMENT_FEE_ID_KEY, PAYMENT_FEE_ID)
            .put(PAYMENT_SITE_ID_KEY, siteId)
            .build();
    }

    private Map<String, Object> mapPaymentFromCCDModel(Map<String, Object> ccdFormatPaymentInfo) {
        Map<String, Object> mapCopy = Maps.newHashMap(ccdFormatPaymentInfo);
        mapCopy.put(PAYMENT_DATE_KEY, ccdUtil.mapCCDDateToDivorceDate(
            (String) ccdFormatPaymentInfo.get(PAYMENT_DATE_KEY)));
        return mapCopy;
    }

    private String getSiteId(Map<String, Object> caseData) throws TaskException {
        return taskCommons.getCourt((String) caseData.get(DIVORCE_UNIT_JSON_KEY)).getSiteId();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> searchSuccessStatePayment(Map<String, Object> divorceCase) {
        return getStreamFromExistingPayments(divorceCase)
            .map(paymentMapElem -> (Map<String, Object>) paymentMapElem.get(PAYMENT_VALUE))
            .filter(Objects::nonNull)
            .filter(paymentObject -> matchPaymentState(SUCCESS_PAYMENT_STATUS, paymentObject))
            .findFirst()
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    private Optional<Map<String, Object>> searchSuccessPaymentOnInitiatedState(String auth,
                                                                               Map<String, Object> divorceCase) {
        String serviceToken = serviceAuthGenerator.generate();

        return getStreamFromExistingPayments(divorceCase)
            .map(paymentMapElem -> (Map<String, Object>) paymentMapElem.get(PAYMENT_VALUE))
            .map(paymentMapElem -> getSuccessPaymentOnInitiatedState(auth, serviceToken, paymentMapElem))
            .filter(Objects::nonNull)
            .findFirst();
    }

    private Map<String, Object> getSuccessPaymentOnInitiatedState(
        String auth,
        String serviceToken,
        Map<String, Object> paymentObject) {

        if (paymentObject != null && INITIATED_PAYMENT_STATUS.equalsIgnoreCase(String.valueOf(
            paymentObject.get(PAYMENT_STATUS)))) {
            Map<String, Object> payment = paymentClient.checkPayment(auth,
                serviceToken,
                (String) paymentObject.get(PAYMENT_REFERENCE));
            if (SUCCESS_PAYMENT_STATUS.equalsIgnoreCase(String.valueOf(payment.get(STATUS_FROM_PAYMENT)))) {
                return payment;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Stream<Map<String, Object>> getStreamFromExistingPayments(Map<String, Object> divorceCase) {
        return Optional.ofNullable((List<Map<String, Object>>) divorceCase.get(D_8_PAYMENTS))
            .orElse(Collections.emptyList())
            .stream();
    }

    private boolean matchPaymentState(String expectedStatus, Map<String, Object> paymentObject) {
        return expectedStatus.equalsIgnoreCase((String) paymentObject.get(PAYMENT_STATUS));
    }
}