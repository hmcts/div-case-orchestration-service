package uk.gov.hmcts.reform.divorce.orchestration.workflows.bailiff;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bailiff.CertificateOfServiceGenerationTask;

import java.util.Map;

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
public class IssueBailiffPackWorkflowTest {

    @Mock
    CertificateOfServiceGenerationTask certificateOfServiceGenerationTask;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @InjectMocks
    private IssueBailiffPackWorkflow issueBailiffPackWorkflow;

    @Test
    public void shouldCallAppropriateTasks_ForPreparingForSubmission() throws WorkflowException {
        Map<String, Object> incomingCaseData = TestConstants.TEST_INCOMING_PAYLOAD;
        Map<String, Object> mockCaseDataToReturn = TestConstants.TEST_PAYLOAD_TO_RETURN;
        when(certificateOfServiceGenerationTask.execute(any(), any())).thenReturn(mockCaseDataToReturn);

        Map<String, Object> returnedCaseData = issueBailiffPackWorkflow.issueCertificateOfServiceDocument("testAuthToken", "123", incomingCaseData);

        assertThat(returnedCaseData, equalTo(mockCaseDataToReturn));
        verify(certificateOfServiceGenerationTask).execute(taskContextArgumentCaptor.capture(), eq(incomingCaseData));
        assertThat(taskContextArgumentCaptor.getValue().getTransientObject(CASE_ID_JSON_KEY), is("123"));
        assertThat(taskContextArgumentCaptor.getValue().getTransientObject(AUTH_TOKEN_JSON_KEY), is("testAuthToken"));
    }
}
