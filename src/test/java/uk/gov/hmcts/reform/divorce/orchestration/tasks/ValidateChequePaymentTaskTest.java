package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_PAYMENT_CHEQUE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
@RequiredArgsConstructor
public class ValidateChequePaymentTaskTest {

    private ValidateChequePaymentTask validateChequePaymentTask;

    @Before
    public void setup() {
        validateChequePaymentTask = new ValidateChequePaymentTask();
    }

    @Test
    public void throwExceptionIfPaymentMethodNotCheque() throws TaskException {
        Map<String, Object> payload = new HashMap<>();
        payload.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(payload)
            .build();

        TaskContext context = context();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        assertThrows("Validation for payment method failed!", TaskException.class, () -> validateChequePaymentTask.execute(context, payload));
    }

    @Test
    public void throwExceptionIfNoPaymentMethodDefined() throws TaskException {
        Map<String, Object> payload = new HashMap<>();

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(payload)
            .build();

        TaskContext context = context();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        assertThrows("No payment method defined!", TaskException.class, () -> validateChequePaymentTask.execute(context, payload));
    }

    @Test
    public void doesNotThrowExceptionIfPaymentMethodIsCheque() throws TaskException {
        Map<String, Object> payload = new HashMap<>();
        payload.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, SOL_PAYMENT_CHEQUE_VALUE);

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(payload)
            .build();

        TaskContext context = context();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        assertFalse(context.hasTaskFailed());
    }
}
