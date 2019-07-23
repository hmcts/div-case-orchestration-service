package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseWithIdTask;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class GetCaseWithIdWorkflowTest {
    @Mock
    private GetCaseWithIdTask getCaseWithId;

    @InjectMocks
    private GetCaseWithIdWorkflow classUnderTest;

    @Test
    public void whenGetCase_thenProcessAsExpected() throws WorkflowException, TaskException {
        final Task[] tasks = new Task[] {
            getCaseWithId
        };

        final CaseDetails caseDetails = CaseDetails.builder().build();

        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        // getCaseWithId should set the caseDetails on context and return a null payload
        Answer<UserDetails> getCaseWithIdAnswer = new Answer<UserDetails>() {
            public UserDetails answer(InvocationOnMock invocation) throws Throwable {
                // Params are passed in as context, payload. First index will have the context
                DefaultTaskContext invokedContext = (DefaultTaskContext) invocation.getArguments()[0];
                invokedContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
                return null;
            }
        };

        when(getCaseWithId.execute(context, null)).thenAnswer(getCaseWithIdAnswer);

        CaseDetails actual = classUnderTest.run(TEST_CASE_ID);

        assertEquals(caseDetails, actual);
    }
}