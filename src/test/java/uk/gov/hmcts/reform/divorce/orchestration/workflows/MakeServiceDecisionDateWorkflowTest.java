package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MakeServiceDecisionDateTask;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;

@RunWith(MockitoJUnitRunner.class)
public class MakeServiceDecisionDateWorkflowTest extends TestCase {

    @Mock
    private MakeServiceDecisionDateTask makeServiceDecisionDateTask;

    @InjectMocks
    private MakeServiceDecisionDateWorkflow makeServiceDecisionDateWorkflow;

    @Test
    public void runShouldBeExecuted() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        mockTasksExecution(caseData, makeServiceDecisionDateTask);

        makeServiceDecisionDateWorkflow.run(CaseDetails.builder().caseData(caseData).build());

        verifyTaskWasCalled(caseData, makeServiceDecisionDateTask);
    }
}
