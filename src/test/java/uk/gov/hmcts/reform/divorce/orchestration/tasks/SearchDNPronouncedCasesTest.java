package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DA_PERIOD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_PRONOUNCED;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.JsonPathMatcher.jsonPathValueMatcher;

@RunWith(MockitoJUnitRunner.class)
public class SearchDNPronouncedCasesTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private SearchDNPronouncedCases classUnderTest;

    private int pageSize;
    private int start;

    private static final String TWO_MINUTES = "2m";
    private String timeSinceDNWasPronounced = TWO_MINUTES;

    private DefaultTaskContext contextBeingModified;

    @Before
    public void setupTaskContextWithSearchSettings() {
        pageSize = 10;
        start = 0;

        Map<String, Object> searchSettings = new HashMap<String, Object>() {{
                put("PAGE_SIZE", pageSize);
                put("FROM", start);
                put(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
                put(AWAITING_DA_PERIOD_KEY, timeSinceDNWasPronounced);
            }};

        contextBeingModified = new DefaultTaskContext();
        contextBeingModified.setTransientObjects(searchSettings);
    }

    @Test
    public void execute_pageSize10_totalResults0() throws TaskException {

        final int totalSearchResults = 0;

        final List<String> expectedCaseIdsInTheContext = Collections.emptyList();

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenReturn(SearchResult.builder()
                .total(totalSearchResults)
                .cases(Collections.emptyList())
                .build());

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
        assertNull(actualResult);

        verify(caseMaintenanceClient).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.filter[*].match.state.query", hasItem(DN_PRONOUNCED))));
    }

    @Test
    public void execute_pageSize10_totalResults5() throws TaskException {

        final int totalSearchResults = 5;

        final List<String> expectedCaseIdsInTheContext = Arrays.asList("1", "2", "3", "4", "5");

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenReturn(SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(0,5))
                .build());

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
        assertNull(actualResult);

        verify(caseMaintenanceClient).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.filter[*].match.state.query", hasItem(DN_PRONOUNCED))));
    }

    @Test
    public void execute_pageSize10_totalResults10() throws TaskException {

        final int totalSearchResults = 10;

        final List<String> expectedCaseIdsInTheContext = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenReturn(SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(0,10))
                .build());

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
        assertNull(actualResult);

        verify(caseMaintenanceClient).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.filter[*].match.state.query", hasItem(DN_PRONOUNCED))));
    }

    @Test
    public void execute_pageSize10_totalResults20() throws TaskException {

        final int totalSearchResults = 20;

        final List<String> expectedCaseIdsInTheContext = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
            "12", "13", "14", "15", "16", "17", "18", "19", "20");

        List<SearchResult> searchResultBatchList = Arrays.asList(
            SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(0,10))
                .build(),
            SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(10,10))
                .build()
            );

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenAnswer(AdditionalAnswers.returnsElementsOf(searchResultBatchList));

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
        assertNull(actualResult);

        verify(caseMaintenanceClient, times(2)).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.filter[*].match.state.query", hasItem(DN_PRONOUNCED))));
    }

    @Test
    public void execute_pageSize10_totalResults30() throws TaskException {

        final int totalSearchResults = 30;

        final List<String> expectedCaseIdsInTheContext = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
            "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30");

        List<SearchResult> searchResultBatchList = Arrays.asList(
            SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(0,10))
                .build(),
            SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(10,10))
                .build(),
            SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(20,10))
                .build()
        );

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenAnswer(AdditionalAnswers.returnsElementsOf(searchResultBatchList));

        final Map<String, Object> actualResult = classUnderTest.execute(contextBeingModified, null);
        assertEquals(expectedCaseIdsInTheContext, contextBeingModified.getTransientObject(SEARCH_RESULT_KEY));
        assertNull(actualResult);

        verify(caseMaintenanceClient, times(3)).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.filter[*].match.state.query", hasItem(DN_PRONOUNCED))));
    }

    @Test
    public void execute_exceptionDuringSearch_searchStops() throws TaskException {
        final int totalSearchResults = 20;

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenReturn(SearchResult.builder()
            .total(totalSearchResults)
            .cases(buildCases(0,10))
            .build())
            .thenThrow(new FeignException.BadRequest("Bad test request", "".getBytes()));

        Map<String, Object> actualResult = null;
        try {
            actualResult = classUnderTest.execute(contextBeingModified, null);
        } catch (FeignException fException) {
            assertThat(fException.status(), CoreMatchers.is(BAD_REQUEST_400));
            assertThat(fException.getMessage(), CoreMatchers.is("Bad test request"));
        }

        assertNull(actualResult);
        verify(caseMaintenanceClient, times(2)).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.filter[*].match.state.query", hasItem(DN_PRONOUNCED))));
    }

    private List<CaseDetails> buildCases(int startId, int caseCount) {
        final List<CaseDetails> cases = new ArrayList<>();

        for (int i = 0; i < caseCount; i++) {
            cases.add(buildCase(startId + 1));
            startId++;
        }
        return cases;
    }

    private CaseDetails buildCase(int caseId) {
        return CaseDetails.builder()
            .caseId(String.valueOf(caseId))
            .build();
    }
}
