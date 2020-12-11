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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.alternativeservice.AlternativeServiceType.SERVED_BY_ALTERNATIVE_METHOD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.alternativeservice.AlternativeServiceType.SERVED_BY_PROCESS_SERVER;

@RunWith(MockitoJUnitRunner.class)
public class AosOverdueForAlternativeServiceCaseWorkflowTest {

    @Mock
    private UpdateCaseInCCD updateCaseInCCD;

    @InjectMocks
    private AosOverdueForAlternativeServiceCaseWorkflow classUnderTest;

    @Captor
    private ArgumentCaptor<TaskContext> contextArgumentCaptor;

    @Test
    public void shouldCallAppropriateTaskForCaseServedByProcessServer() throws WorkflowException {
        classUnderTest.run(AUTH_TOKEN, TEST_CASE_ID, SERVED_BY_PROCESS_SERVER);

        verify(updateCaseInCCD).execute(contextArgumentCaptor.capture(), eq(emptyMap()));
        TaskContext taskContext = contextArgumentCaptor.getValue();
        assertThat(taskContext.getTransientObject(AUTH_TOKEN_JSON_KEY), is(AUTH_TOKEN));
        assertThat(taskContext.getTransientObject(CASE_ID_JSON_KEY), is(TEST_CASE_ID));
        assertThat(taskContext.getTransientObject(CASE_EVENT_ID_JSON_KEY), is("aosNotReceivedForProcessServer"));
    }

    @Test
    public void shouldCallAppropriateTaskForCaseServedByAlternativeMethod() throws WorkflowException {
        classUnderTest.run(AUTH_TOKEN, TEST_CASE_ID, SERVED_BY_ALTERNATIVE_METHOD);

        verify(updateCaseInCCD).execute(contextArgumentCaptor.capture(), eq(emptyMap()));
        TaskContext taskContext = contextArgumentCaptor.getValue();
        assertThat(taskContext.getTransientObject(AUTH_TOKEN_JSON_KEY), is(AUTH_TOKEN));
        assertThat(taskContext.getTransientObject(CASE_ID_JSON_KEY), is(TEST_CASE_ID));
        assertThat(taskContext.getTransientObject(CASE_EVENT_ID_JSON_KEY), is("aosNotReceivedForAltMethod"));
    }

    @Test
    public void shouldThrowExceptionWhenAlternativeServiceTypeIsUnexpected() {
        WorkflowException workflowException = assertThrows(WorkflowException.class, () -> classUnderTest.run(AUTH_TOKEN, TEST_CASE_ID, null));

        assertThat(workflowException.getMessage(), is("Case id test.case.id. Unexpected value for alternative service type [null]"));
    }

}