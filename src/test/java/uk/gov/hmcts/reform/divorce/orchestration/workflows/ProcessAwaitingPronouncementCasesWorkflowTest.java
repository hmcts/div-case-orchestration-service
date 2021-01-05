package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.BulkCaseCreateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SearchAwaitingPronouncementCases;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDivorceCaseWithinBulk;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class ProcessAwaitingPronouncementCasesWorkflowTest {

    @Mock
    private SearchAwaitingPronouncementCases searchAwaitingPronouncementCasesMock;

    @Mock
    private BulkCaseCreateTask createBulkCaseMock;

    @Mock
    private UpdateDivorceCaseWithinBulk updateDivorceCaseWithinBulkMock;

    @InjectMocks
    private ProcessAwaitingPronouncementCasesWorkflow classUnderTest;

    @Test
    public void whenProcessAwaitingPronouncement_thenProcessAsExpected() throws WorkflowException {
        final ImmutablePair<String, Object> authTokenPair = new ImmutablePair<>(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        final Task[] tasks = new Task[] {
            searchAwaitingPronouncementCasesMock,
            createBulkCaseMock,
            updateDivorceCaseWithinBulkMock
        };
        when(classUnderTest.execute(tasks, null, authTokenPair)).thenReturn(emptyMap());

        Map<String, Object> returnedCaseData = classUnderTest.run(AUTH_TOKEN);

        assertThat(returnedCaseData, is(emptyMap()));
    }

}
