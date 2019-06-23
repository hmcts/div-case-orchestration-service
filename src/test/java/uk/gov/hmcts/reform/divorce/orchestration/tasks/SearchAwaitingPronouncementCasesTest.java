package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.JsonPathMatcher.jsonPathValueMatcher;

@RunWith(MockitoJUnitRunner.class)
public class SearchAwaitingPronouncementCasesTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private SearchAwaitingPronouncementCases classUnderTest;

    @Test
    public void givenCasesExists_whenSearchCases_thenReturnExpectedOutput() throws TaskException {
        final DefaultTaskContext context = new DefaultTaskContext();

        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        classUnderTest.setPageSize(10);
        final SearchResult cmsSearchResponse =
                SearchResult.builder()
                    .cases(Collections.emptyList())
                    .build();

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any())).thenReturn(cmsSearchResponse);

        classUnderTest.execute(context, null);

        Object actual = context.getTransientObject(SEARCH_RESULT_KEY);

        assertEquals(cmsSearchResponse.getCases(), actual);

        verify(caseMaintenanceClient).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.must[*].match.state.query", hasItem(AWAITING_PRONOUNCEMENT))));
        verify(caseMaintenanceClient).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.must_not[*].exists.field", hasItems("data.BulkListingCaseId", "data.DateAndTimeOfHearing"))));
        verify(caseMaintenanceClient).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.must[*].exists.field", hasItem("data.DnOutcomeCase"))));
    }
}
