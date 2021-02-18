package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_PAYMENT_CHEQUE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Component
@Slf4j
public class ValidateChequePaymentTask implements Task<Map<String, Object>> {

    @Autowired
    CcdUtil ccdUtil;

    @Override
    public Map<String, Object> execute(TaskContext context,  Map<String, Object> payload) {

        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        String solPaymentMethod = getOptionalPropertyValueAsString(caseDetails.getCaseData(), SOLICITOR_HOW_TO_PAY_JSON_KEY, "");

        if (solPaymentMethod.equals("")) {
            log.error("Case ID {}: validation failed for payment method, No payment method found!", caseDetails.getCaseId());
            throw new TaskException("No payment method defined!");
        } else if (!SOL_PAYMENT_CHEQUE_VALUE.equalsIgnoreCase(solPaymentMethod)) {
            log.error("Case ID {}: validation failed for payment method, payment method is not cheque", caseDetails.getCaseId());
            throw new TaskException("Validation for payment method failed!");
        }

        return payload;
    }

}