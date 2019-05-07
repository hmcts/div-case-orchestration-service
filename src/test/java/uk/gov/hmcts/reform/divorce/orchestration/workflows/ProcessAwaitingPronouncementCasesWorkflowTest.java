package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SearchAwaitingPronouncementCases;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEARCH_PAGE_KEY;

@RunWith(MockitoJUnitRunner.class)
public class ProcessAwaitingPronouncementCasesWorkflowTest {

    @Mock
    private SearchAwaitingPronouncementCases searchAwaitingPronouncementCases;

    @InjectMocks
    private ProcessAwaitingPronouncementCasesWorkflow classUnderTest;

    @Test
    public void whenProcessAwaitingPronouncement_thenProcessAsExpected() throws WorkflowException {
        final ImmutablePair<String, Object> authTokenPair = new ImmutablePair<>(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        final ImmutablePair<String, Integer> searchPageKayPair = new ImmutablePair<>(SEARCH_PAGE_KEY, 0);

        final Task[] tasks = new Task[]{
                searchAwaitingPronouncementCases
        };

        final SearchResult searchResult = SearchResult.builder().build();

        when(classUnderTest.execute(tasks, null, authTokenPair, searchPageKayPair)).thenReturn(searchResult);

        SearchResult actual = classUnderTest.run(AUTH_TOKEN);

        assertEquals(searchResult, actual);
    }
}
