package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EXISTING_PAYMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.INITIATED_PAYMENT_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SESSION_PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_PAYMENT_STATUS;

@Component
@Slf4j
public class SetPaymentOnSession implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String caseId = String.valueOf(context.getTransientObject(CASE_ID_JSON_KEY));
        String paymentRef = getPaymentByStatus(caseData, SUCCESS_PAYMENT_STATUS);

        if (StringUtils.isEmpty(paymentRef)) {
            paymentRef = getPaymentByStatus(caseData, INITIATED_PAYMENT_STATUS);
        }


        if (StringUtils.isNotEmpty(paymentRef)) {
            log.info("Case Id {} has successful payment with ref {}", caseId, paymentRef);
            caseData.put(SESSION_PAYMENT_REFERENCE, paymentRef);
        }

        return caseData;
    }

    @SuppressWarnings("unchecked")
    private String getPaymentByStatus(Map<String, Object> divorceCase,final  String status) {
        Map<String, Object> paymentElement = (Map<String, Object>) divorceCase.get(PAYMENT);

        String casePaymentRef = null;
        if (paymentElement != null && isPaymentInStatus(paymentElement, status)) {
            casePaymentRef = (String) paymentElement.get(PAYMENT_REFERENCE);
        }

        if (StringUtils.isEmpty(casePaymentRef)) {

            Stream<Map<String, Object>> paymentMaps = Optional
                    .ofNullable((List<Map<String, Object>>) divorceCase.get(EXISTING_PAYMENTS))
                    .orElse(Collections.emptyList())
                    .stream();
            casePaymentRef = paymentMaps
                    .map(paymentMapElem -> (Map<String, Object>)paymentMapElem.get(PAYMENT_VALUE))
                    .filter(Objects::nonNull)
                    .filter(paymentObject -> isPaymentInStatus(paymentObject, status))
                    .findFirst()
                    .map(successPayment -> (String)successPayment.get(PAYMENT_REFERENCE))
                    .orElse(Strings.EMPTY);
        }
        return casePaymentRef;

    }

    private boolean isPaymentInStatus(Map<String, Object> paymentObject, String expectedState) {
        return expectedState.equalsIgnoreCase(String.valueOf(paymentObject.get(PAYMENT_STATUS)));
    }

}