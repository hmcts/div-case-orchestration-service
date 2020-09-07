package uk.gov.hmcts.reform.divorce.orchestration.workflows.aos;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aos.AosOverdueCoverLetterGenerationTask;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class AosNotReceivedWorkflowTest {

    @Mock
    private AosOverdueCoverLetterGenerationTask coverLetterGenerationTask;

    @InjectMocks
    private AosNotReceivedWorkflow classUnderTest;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @Test
    public void shouldCallAppropriateTasks_ForPreparingForSubmission() throws WorkflowException {
        Map<String, Object> incomingCaseData = singletonMap("incomingKey", "incomingValue");
        Map<String, Object> mockCaseDataToReturn = singletonMap("returnedKey", "returnedValue");
        when(coverLetterGenerationTask.execute(any(), any())).thenReturn(mockCaseDataToReturn);

        Map<String, Object> returnedCaseData = classUnderTest.prepareForSubmission("testAuthToken", "123", incomingCaseData);

        assertThat(returnedCaseData, equalTo(mockCaseDataToReturn));
        verify(coverLetterGenerationTask).execute(taskContextArgumentCaptor.capture(), eq(incomingCaseData));
        assertThat(taskContextArgumentCaptor.getValue().getTransientObject(CASE_ID_JSON_KEY), is("123"));
        assertThat(taskContextArgumentCaptor.getValue().getTransientObject(AUTH_TOKEN_JSON_KEY), is("testAuthToken"));
    }

}