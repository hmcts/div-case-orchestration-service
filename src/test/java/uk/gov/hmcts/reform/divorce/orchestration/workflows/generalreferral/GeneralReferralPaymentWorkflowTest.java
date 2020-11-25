package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.FurtherHWFPaymentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.FurtherPBAPaymentTask;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.FEE_ACCOUNT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.HELP_WITH_FEE_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasNeverCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralPaymentWorkflowTest {

    @Mock
    private FurtherPBAPaymentTask furtherPBAPaymentTask;

    @Mock
    private FurtherHWFPaymentTask furtherHWFPaymentTask;

    @InjectMocks
    private GeneralReferralPaymentWorkflow generalReferralPaymentWorkflow;

    @Test
    public void shouldExecuteOnlyFurtherPBAPaymentTask() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE, FEE_ACCOUNT_TYPE);

        mockTasksExecution(caseData, furtherPBAPaymentTask);

        generalReferralPaymentWorkflow.run(CaseDetails.builder().caseData(caseData).build());

        verifyTaskWasCalled(
            caseData,
            furtherPBAPaymentTask);

        verifyTaskWasNeverCalled(furtherHWFPaymentTask);
    }

    @Test
    public void shouldExecuteOnlyFurtherHWFPaymentTask() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE, HELP_WITH_FEE_TYPE);

        mockTasksExecution(caseData, furtherHWFPaymentTask);

        generalReferralPaymentWorkflow.run(CaseDetails.builder().caseData(caseData).build());

        verifyTaskWasCalled(
            caseData,
            furtherHWFPaymentTask);

        verifyTaskWasNeverCalled(furtherPBAPaymentTask);
    }

    @Test
    public void shouldExecuteNoFurtherPaymentTask() throws Exception {
        Map<String, Object> caseData = new HashMap<>();

        generalReferralPaymentWorkflow.run(CaseDetails.builder().caseData(caseData).build());

        verifyTasksWereNeverCalled(furtherPBAPaymentTask, furtherHWFPaymentTask);
    }

}