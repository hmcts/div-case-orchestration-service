package uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.DN_PRONOUNCED;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.JsonPathMatcher.jsonPathValueMatcher;
import static uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchUtils.EMPTY_SEARCH_RESULT;

/**
 * This class is meant to test the helper class and makes sure pagination works for Elastic Search queries.
 */
@RunWith(MockitoJUnitRunner.class)
public class CMSElasticSearchSupportTest {

    private static final QueryBuilder TEST_QUERY_BUILDER = matchQuery("state", DN_PRONOUNCED);

    @Mock
    private CaseMaintenanceClient mockCaseMaintenanceClient;

    @InjectMocks
    private CMSElasticSearchSupport classUnderTest;

    private CMSElasticSearchIterator cmsElasticSearchIterator;

    @Before
    public void setUp() {
        cmsElasticSearchIterator = classUnderTest.createNewCMSElasticSearchIterator(AUTH_TOKEN, TEST_QUERY_BUILDER);
    }

    @Test
    public void shouldReturnExpectedCasesWhenSearchingWithAnIterator() {
        final List<String> expectedCaseIds = asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
            "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25");
        when(mockCaseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any())).thenReturn(
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
                .cases(buildCases(20, 5))
                .build()
        );

        List<String> foundCaseDetails = new ArrayList<>();
        CMSElasticSearchSupport.searchTransformAndProcessCMSElasticSearchCases(cmsElasticSearchIterator,
            caseDetails -> Optional.ofNullable(caseDetails.getCaseId()),
            foundCaseDetails::add
        );

        assertThat(foundCaseDetails, equalTo(expectedCaseIds));
        verify(mockCaseMaintenanceClient, times(3)).searchCases(eq(AUTH_TOKEN),
            argThat(jsonPathValueMatcher("$.query.match.state.query", is(DN_PRONOUNCED))));
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

        String actualSearchSource = CMSElasticSearchSupport.buildCMSBooleanSearchSource(0, 10, QueryBuilders.boolQuery().filter(firstQuery));

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

        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(firstQuery)
            .filter(secondQuery)
            .filter(thirdQuery);
        String actualSearchSource = CMSElasticSearchSupport.buildCMSBooleanSearchSource(0, 5, queryBuilder);

        assertEquals(expectedSearchSource.toString(), actualSearchSource);
    }

    @Test
    public void shouldCreateNewInstanceOfCMSElasticSearchIterator() {
        when(mockCaseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any())).thenReturn(EMPTY_SEARCH_RESULT);

        CMSElasticSearchIterator firstCmsElasticSearchIterator = classUnderTest.createNewCMSElasticSearchIterator(AUTH_TOKEN, TEST_QUERY_BUILDER);
        CMSElasticSearchIterator secondCmsElasticSearchIterator = classUnderTest.createNewCMSElasticSearchIterator(AUTH_TOKEN, TEST_QUERY_BUILDER);
        firstCmsElasticSearchIterator.fetchNextBatch();
        secondCmsElasticSearchIterator.fetchNextBatch();

        assertThat(firstCmsElasticSearchIterator, is(notNullValue()));
        assertThat(secondCmsElasticSearchIterator, is(notNullValue()));
        assertThat(firstCmsElasticSearchIterator, is(not(sameInstance(secondCmsElasticSearchIterator))));
        verify(mockCaseMaintenanceClient, times(2)).searchCases(eq(AUTH_TOKEN), any());
    }

    private List<CaseDetails> buildCases(int startId, int caseCount) {
        final List<CaseDetails> cases = new ArrayList<>();

        for (int i = 1; i <= caseCount; i++) {
            cases.add(CaseDetails.builder().caseId(String.valueOf(startId + i)).build());
        }

        return cases;
    }

}