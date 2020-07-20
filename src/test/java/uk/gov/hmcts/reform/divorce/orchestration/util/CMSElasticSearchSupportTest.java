package uk.gov.hmcts.reform.divorce.orchestration.util;

import feign.FeignException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_PRONOUNCED;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.JsonPathMatcher.jsonPathValueMatcher;
import static wiremock.org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;

/**
 * This class is meant to test the helper class and makes sure pagination works for Elastic Search queries.
 */
@RunWith(MockitoJUnitRunner.class)
public class CMSElasticSearchSupportTest {

    private static final QueryBuilder TEST_QUERY_BUILDER = matchQuery("state", DN_PRONOUNCED);

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private CMSElasticSearchSupport classUnderTest;

    @Test
    public void execute_pageSize10_totalResults0() {
        final List expectedCaseIds = Collections.emptyList();
        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenReturn(SearchResult.builder()
                .total(expectedCaseIds.size())
                .cases(Collections.emptyList())
                .build());

        List<CaseDetails> caseDetails = classUnderTest.searchCMSCases(AUTH_TOKEN, TEST_QUERY_BUILDER).collect(Collectors.toList());
        assertThat(caseDetails, hasSize(0));
        assertThat(caseDetails, equalTo(expectedCaseIds));

        verify(caseMaintenanceClient).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.filter[*].match.state.query", hasItem(DN_PRONOUNCED))));
    }

    @Test
    public void execute_pageSize10_totalResults5() {
        final List<String> expectedCaseIds = asList("1", "2", "3", "4", "5");
        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenReturn(SearchResult.builder()
                .total(expectedCaseIds.size())
                .cases(buildCases(0, 5))
                .build());

        List<String> caseDetails = classUnderTest.searchCMSCases(AUTH_TOKEN, TEST_QUERY_BUILDER)
            .map(CaseDetails::getCaseId)
            .collect(Collectors.toList());
        assertThat(caseDetails, equalTo(expectedCaseIds));

        verify(caseMaintenanceClient).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.filter[*].match.state.query", hasItem(DN_PRONOUNCED))));
    }

    @Test
    public void execute_pageSize10_totalResults10() {
        final List<String> expectedCaseIds = asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenReturn(SearchResult.builder()
                .total(expectedCaseIds.size())
                .cases(buildCases(0, 10))
                .build());

        List<String> caseDetails = classUnderTest.searchCMSCases(AUTH_TOKEN, TEST_QUERY_BUILDER)
            .map(CaseDetails::getCaseId)
            .collect(Collectors.toList());
        assertThat(caseDetails, equalTo(expectedCaseIds));

        verify(caseMaintenanceClient).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.filter[*].match.state.query", hasItem(DN_PRONOUNCED))));
    }

    @Test
    public void execute_pageSize10_totalResults20() {
        final List<String> expectedCaseIds = asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
            "12", "13", "14", "15", "16", "17", "18", "19", "20");
        List<SearchResult> searchResultBatchList = asList(
            SearchResult.builder()
                .total(expectedCaseIds.size())
                .cases(buildCases(0, 10))
                .build(),
            SearchResult.builder()
                .total(expectedCaseIds.size())
                .cases(buildCases(10, 10))
                .build()
        );

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenAnswer(AdditionalAnswers.returnsElementsOf(searchResultBatchList));

        List<String> caseDetails = classUnderTest.searchCMSCases(AUTH_TOKEN, TEST_QUERY_BUILDER)
            .map(CaseDetails::getCaseId)
            .collect(Collectors.toList());
        assertThat(caseDetails, equalTo(expectedCaseIds));

        verify(caseMaintenanceClient, times(2)).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.filter[*].match.state.query", hasItem(DN_PRONOUNCED))));
    }

    @Test
    public void execute_pageSize10_totalResults30() {
        final List<String> expectedCaseIds = asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
            "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30");
        List<SearchResult> searchResultBatchList = asList(
            SearchResult.builder()
                .total(expectedCaseIds.size())
                .cases(buildCases(0, 10))
                .build(),
            SearchResult.builder()
                .total(expectedCaseIds.size())
                .cases(buildCases(10, 10))
                .build(),
            SearchResult.builder()
                .total(expectedCaseIds.size())
                .cases(buildCases(20, 10))
                .build()
        );

        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenAnswer(AdditionalAnswers.returnsElementsOf(searchResultBatchList));

        List<String> caseDetails = classUnderTest.searchCMSCases(AUTH_TOKEN, TEST_QUERY_BUILDER)
            .map(CaseDetails::getCaseId)
            .collect(Collectors.toList());
        assertThat(caseDetails, equalTo(expectedCaseIds));

        verify(caseMaintenanceClient, times(3)).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.filter[*].match.state.query", hasItem(DN_PRONOUNCED))));
    }

    @Test
    public void execute_exceptionDuringSearch_searchStops() {
        final int totalSearchResults = 20;
        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any()))
            .thenReturn(SearchResult.builder()
                .total(totalSearchResults)
                .cases(buildCases(0, 10))
                .build())
            .thenThrow(new FeignException.BadRequest("Bad test request", "".getBytes()));

        List<String> caseDetails = null;
        try {
            caseDetails = classUnderTest.searchCMSCases(AUTH_TOKEN, TEST_QUERY_BUILDER)
                .map(CaseDetails::getCaseId)
                .collect(Collectors.toList());
        } catch (FeignException fException) {
            assertThat(fException.status(), is(BAD_REQUEST_400));
            assertThat(fException.getMessage(), is("Bad test request"));
        }

        assertNull(caseDetails);
        verify(caseMaintenanceClient, times(2)).searchCases(eq(AUTH_TOKEN), argThat(
            jsonPathValueMatcher("$.query.bool.filter[*].match.state.query", hasItem(DN_PRONOUNCED))));
    }

    @Test
    public void shouldBuildDateForGivenTimeLimit() {
        String limitDateParameter = CMSElasticSearchSupport.buildDateForTodayMinusGivenPeriod("1y");
        assertThat(limitDateParameter, is("now/y-1y"));
    }

    @Test
    public void buildCMSBooleanSearchSource_givenOneQuery() {
        int pageSize = 10;
        int start = 0;
        QueryBuilder firstQuery = QueryBuilders.matchQuery("testKey", "AwaitingDA");

        QueryBuilder testQuery = QueryBuilders
            .boolQuery()
            .filter(firstQuery);

        SearchSourceBuilder expectedSearchSource = SearchSourceBuilder
            .searchSource()
            .query(testQuery)
            .from(start)
            .size(pageSize);

        String actualSearchSource = CMSElasticSearchSupport.buildCMSBooleanSearchSource(0, 10, firstQuery);

        assertEquals(expectedSearchSource.toString(), actualSearchSource);
    }

    @Test
    public void buildCMSBooleanSearchSource_givenThreeQueries() {
        int pageSize = 5;
        int start = 0;
        QueryBuilder firstQuery = QueryBuilders.matchQuery("testKey", "AwaitingDA");
        QueryBuilder secondQuery = QueryBuilders.existsQuery("EXIST_KEY");
        QueryBuilder thirdQuery = QueryBuilders.rangeQuery("DATE_KEY").lte("2019-11-02");


        QueryBuilder testQuery = QueryBuilders
            .boolQuery()
            .filter(firstQuery)
            .filter(secondQuery)
            .filter(thirdQuery);

        SearchSourceBuilder expectedSearchSource = SearchSourceBuilder
            .searchSource()
            .query(testQuery)
            .from(start)
            .size(pageSize);

        String actualSearchSource = CMSElasticSearchSupport.buildCMSBooleanSearchSource(0, 5, firstQuery, secondQuery, thirdQuery);

        assertEquals(expectedSearchSource.toString(), actualSearchSource);
    }

    private List<CaseDetails> buildCases(int startId, int caseCount) {
        final List<CaseDetails> cases = new ArrayList<>();

        for (int i = 1; i <= caseCount; i++) {
            cases.add(CaseDetails.builder().caseId(String.valueOf(startId + i)).build());
        }

        return cases;
    }

}