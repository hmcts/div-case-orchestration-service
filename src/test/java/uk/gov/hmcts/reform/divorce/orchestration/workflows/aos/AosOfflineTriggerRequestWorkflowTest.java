package uk.gov.hmcts.reform.divorce.orchestration.workflows.aos;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aos.AosOfflineTriggerRequestTask;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class AosOfflineTriggerRequestWorkflowTest {

    @Mock
    private AosOfflineTriggerRequestTask aosOfflineTriggerRequestTask;

    @InjectMocks
    private AosOfflineTriggerRequestWorkflow classUnderTest;

    @Test
    public void shouldCallAppropriateTasks() throws WorkflowException {
        classUnderTest.requestAosOfflineToBeTriggered(TEST_CASE_ID);

        verify(aosOfflineTriggerRequestTask).execute(any(), eq(TEST_CASE_ID));
    }

}