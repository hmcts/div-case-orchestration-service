package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.JsonPathMatcher.jsonPathExisteMatcher;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.JsonPathMatcher.jsonPathValueMatcher;

@RunWith(MockitoJUnitRunner.class)
public class SearchAwaitingPronouncementCasesTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private SearchAwaitingPronouncementCases classUnderTest;

    @Test
    public void givenCasesExists_whenSearchCases_thenReturnExpectedOutput() {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        classUnderTest.setPageSize(2);
        final SearchResult cmsSearchResponse =
            SearchResult.builder()
                .total(3)
                .cases(Arrays.asList(CaseDetails.builder().build(), CaseDetails.builder().build()))
                .build();

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any())).thenReturn(cmsSearchResponse);

        classUnderTest.execute(context, null);

        Object actual = context.getTransientObject(SEARCH_RESULT_KEY);

        assertThat(actual, is(Arrays.asList(cmsSearchResponse, cmsSearchResponse)));

        int expectedIterations = 2;

        for (int i = 0; i < expectedIterations; i++) {
            int expectedFrom = i * 2;
            verify(caseMaintenanceClient).searchCases(eq(AUTH_TOKEN), argThat(
                jsonPathValueMatcher("$.from", is(expectedFrom))));
        }

        verify(caseMaintenanceClient, times(expectedIterations)).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.must[*].match.state.query", hasItem(AWAITING_PRONOUNCEMENT))));
        verify(caseMaintenanceClient, times(expectedIterations)).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.must_not[*].exists.field", hasItems("data.BulkListingCaseId", "data.DateAndTimeOfHearing"))));
        verify(caseMaintenanceClient, times(expectedIterations)).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.must[*].exists.field", hasItem("data.DnOutcomeCase"))));
        verify(caseMaintenanceClient, times(expectedIterations)).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathExisteMatcher("$.sort[*].['data.DNApprovalDate']")));
    }
}
