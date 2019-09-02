package uk.gov.hmcts.reform.divorce.orchestration.util;

import feign.FeignException;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.transformation.CaseDetailsMapper;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.transformation.CaseIdMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_PRONOUNCED;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.JsonPathMatcher.jsonPathValueMatcher;
import static wiremock.org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;

/**
 * This class is meant to test the helper class and makes sure pagination works for Elastic Search queries.
 */
@RunWith(MockitoJUnitRunner.class)
public class CMSHelperTest {

    private static final QueryBuilder TEST_QUERY_BUILDER = matchQuery("state", DN_PRONOUNCED);
    private static final CaseDetailsMapper TEST_CASE_DETAILS_MAPPER = new CaseIdMapper();

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    private CMSHelper classUnderTest;

    private int pageSize;
    private int start;

    private DefaultTaskContext contextBeingModified;

    @Before
    public void setupTaskContextWithSearchSettings() {
        classUnderTest = new CMSHelper(caseMaintenanceClient, TEST_CASE_DETAILS_MAPPER);

        pageSize = 10;
        start = 0;

        Map<String, Object> searchSettings = new HashMap<String, Object>() {
            {
                put("PAGE_SIZE", pageSize);
                put("FROM", start);
            }
        };

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

        List<String> transformedCases = classUnderTest.searchCMSCases(start, pageSize, AUTH_TOKEN, TEST_QUERY_BUILDER);
        assertThat(transformedCases, hasSize(0));
        assertThat(transformedCases, equalTo(expectedCaseIdsInTheContext));

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
                .cases(buildCases(0, 5))
                .build());

        List<String> transformedCases = classUnderTest.searchCMSCases(start, pageSize, AUTH_TOKEN, TEST_QUERY_BUILDER);
        assertThat(transformedCases, equalTo(expectedCaseIdsInTheContext));

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
                .cases(buildCases(0, 10))
                .build());

        List<String> transformedCases = classUnderTest.searchCMSCases(start, pageSize, AUTH_TOKEN, TEST_QUERY_BUILDER);
        assertThat(transformedCases, equalTo(expectedCaseIdsInTheContext));

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
                .cases(buildCases(0, 10))
                .build(),
            SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(10, 10))
                .build()
        );

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenAnswer(AdditionalAnswers.returnsElementsOf(searchResultBatchList));

        List<String> transformedCases = classUnderTest.searchCMSCases(start, pageSize, AUTH_TOKEN, TEST_QUERY_BUILDER);
        assertThat(transformedCases, equalTo(expectedCaseIdsInTheContext));

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
                .cases(buildCases(0, 10))
                .build(),
            SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(10, 10))
                .build(),
            SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(20, 10))
                .build()
        );

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenAnswer(AdditionalAnswers.returnsElementsOf(searchResultBatchList));

        List<String> transformedCases = classUnderTest.searchCMSCases(start, pageSize, AUTH_TOKEN, TEST_QUERY_BUILDER);
        assertThat(transformedCases, equalTo(expectedCaseIdsInTheContext));

        verify(caseMaintenanceClient, times(3)).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.filter[*].match.state.query", hasItem(DN_PRONOUNCED))));
    }

    @Test
    public void execute_exceptionDuringSearch_searchStops() throws TaskException {
        final int totalSearchResults = 20;

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenReturn(SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(0, 10))
                .build())
            .thenThrow(new FeignException.BadRequest("Bad test request", "".getBytes()));

        List<String> transformedCases = null;
        try {
            transformedCases = classUnderTest.searchCMSCases(start, pageSize, AUTH_TOKEN, TEST_QUERY_BUILDER);
        } catch (FeignException fException) {
            assertThat(fException.status(), is(BAD_REQUEST_400));
            assertThat(fException.getMessage(), is("Bad test request"));
        }

        assertNull(transformedCases);
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