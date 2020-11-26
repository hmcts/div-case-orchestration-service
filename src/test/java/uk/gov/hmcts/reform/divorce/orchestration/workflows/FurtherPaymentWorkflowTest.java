package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.FurtherHWFPaymentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.FurtherPBAPaymentTask;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.FurtherPaymentWorkflow;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.FEE_ACCOUNT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.HELP_WITH_FEE_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasNeverCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class FurtherPaymentWorkflowTest {

    @Mock
    private FurtherPBAPaymentTask furtherPBAPaymentTask;

    @Mock
    private FurtherHWFPaymentTask furtherHWFPaymentTask;

    @InjectMocks
    private FurtherPaymentWorkflow furtherPaymentWorkflow;

    @Test
    public void shouldExecuteOnlyFurtherPBAPaymentTask() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(GENERAL_REFERRAL_PAYMENT_TYPE, FEE_ACCOUNT_TYPE);

        mockTasksExecution(caseData, furtherPBAPaymentTask);

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        furtherPaymentWorkflow.run(caseDetails, GENERAL_REFERRAL_PAYMENT_TYPE);

        verifyTaskWasCalled(
            caseData,
            furtherPBAPaymentTask);

        verifyTaskWasNeverCalled(furtherHWFPaymentTask);
    }

    @Test
    public void shouldExecuteOnlyFurtherHWFPaymentTask() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(GENERAL_REFERRAL_PAYMENT_TYPE, HELP_WITH_FEE_TYPE);

        mockTasksExecution(caseData, furtherHWFPaymentTask);

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        furtherPaymentWorkflow.run(caseDetails, GENERAL_REFERRAL_PAYMENT_TYPE);

        verifyTaskWasCalled(
            caseData,
            furtherHWFPaymentTask);

        verifyTaskWasNeverCalled(furtherPBAPaymentTask);
    }

    @Test
    public void shouldExecuteNoFurtherPaymentTask() throws Exception {
        Map<String, Object> caseData = new HashMap<>();

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        furtherPaymentWorkflow.run(caseDetails, GENERAL_REFERRAL_PAYMENT_TYPE);

        verifyTasksWereNeverCalled(furtherPBAPaymentTask, furtherHWFPaymentTask);
    }

}