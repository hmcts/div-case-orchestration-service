package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddCourtsToPayloadTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataToDivorceFormatterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrdersFilterTask;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PAYLOAD_TO_RETURN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CASE_ID_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CASE_STATE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.COURT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class GetCaseWorkflowTest {

    @Mock
    private GetCaseTask getCaseTask;

    @Mock
    private GeneralOrdersFilterTask generalOrdersFilterTask;

    @Mock
    private CaseDataToDivorceFormatterTask caseDataToDivorceFormatterTask;

    @Mock
    private AddCourtsToPayloadTask addCourtsToPayloadTask;

    @InjectMocks
    private GetCaseWorkflow classUnderTest;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @Test
    public void whenGetCase_thenProcessAsExpected() throws WorkflowException, TaskException {
        Map<String, Object> payloadToReturn = TEST_PAYLOAD_TO_RETURN;
        when(getCaseTask.execute(any(), isNull()))
            .thenAnswer(invocation -> {
                TaskContext taskContext = invocation.getArgument(0, TaskContext.class);

                taskContext.setTransientObject(CASE_ID_KEY, TEST_CASE_ID);
                taskContext.setTransientObject(CASE_STATE_KEY, TEST_STATE);
                taskContext.setTransientObject(COURT_KEY, TEST_COURT);

                return payloadToReturn;
            });
        mockTasksExecution(payloadToReturn, generalOrdersFilterTask, caseDataToDivorceFormatterTask, addCourtsToPayloadTask);

        Map<String, Object> returnedCaseData = classUnderTest.run(AUTH_TOKEN);

        assertThat(returnedCaseData, is(payloadToReturn));
        assertThat(classUnderTest.getCaseId(), is(TEST_CASE_ID));
        assertThat(classUnderTest.getCaseState(), is(TEST_STATE));
        assertThat(classUnderTest.getCourt(), is(TEST_COURT));

        verify(getCaseTask).execute(taskContextArgumentCaptor.capture(), isNull());
        TaskContext originatingTaskContext = taskContextArgumentCaptor.getValue();
        assertThat(originatingTaskContext.getTransientObject(AUTH_TOKEN_JSON_KEY), is(AUTH_TOKEN));
        verifyTasksCalledInOrder(payloadToReturn, generalOrdersFilterTask, caseDataToDivorceFormatterTask, addCourtsToPayloadTask);
    }

}