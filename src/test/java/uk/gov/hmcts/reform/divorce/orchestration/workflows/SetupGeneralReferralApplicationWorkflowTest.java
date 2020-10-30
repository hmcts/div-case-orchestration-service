package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralApplicationFeeLookupTask;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral.SetupGeneralReferralPaymentWorkflow;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;

@RunWith(MockitoJUnitRunner.class)
public class SetupGeneralReferralApplicationWorkflowTest {

    @Mock
    private GeneralReferralApplicationFeeLookupTask generalReferralApplicationFeeLookupTask;

    @InjectMocks
    private SetupGeneralReferralPaymentWorkflow setupGeneralReferralPaymentWorkflow;

    @Test
    public void whenGeneralApplicationWithoutNoticeFee_thenProcessAsExpected() throws Exception {
        HashMap<String, Object> caseData = new HashMap<>();
        mockTasksExecution(caseData, generalReferralApplicationFeeLookupTask);

        Map<String, Object> returned = setupGeneralReferralPaymentWorkflow.run(
            CaseDetails.builder()
                .caseData(caseData)
                .build()
        );

        assertThat(returned, is(caseData));
        verifyTaskWasCalled(caseData, generalReferralApplicationFeeLookupTask);
    }
}
