package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralReturnToPreviousStateValidationTask;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;

@RunWith(MockitoJUnitRunner.class)
public class ValidateReturnToStateBeforeGeneralReferralWorkflowTest extends TestCase {

    @Mock
    private GeneralReferralReturnToPreviousStateValidationTask generalReferralReturnToPreviousStateValidationTask;

    @InjectMocks
    private ValidateReturnToStateBeforeGeneralReferralWorkflow validateReturnToStateBeforeGeneralReferralWorkflow;

    @Test
    public void runShouldBeExecutedAndCallExpectedTasks() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        mockTasksExecution(caseData, generalReferralReturnToPreviousStateValidationTask);

        validateReturnToStateBeforeGeneralReferralWorkflow.run(CaseDetails.builder().caseData(caseData).build());

        verifyTaskWasCalled(caseData, generalReferralReturnToPreviousStateValidationTask);
    }
}
