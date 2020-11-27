package uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchSupport.buildCMSBooleanSearchSource;
import static uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchUtils.EMPTY_SEARCH_RESULT;

public class CMSElasticSearchIteratorTest {

    public static final int TEST_BATCH_SIZE = 2;
    public static final QueryBuilder TEST_QUERY_BUILDER = QueryBuilders.existsQuery("TestField");

    private CaseMaintenanceClient caseMaintenanceClient;
    private CMSElasticSearchIterator cmsElasticSearchIterator;

    @Before
    public void setUp() {
        caseMaintenanceClient = mock(CaseMaintenanceClient.class);
        cmsElasticSearchIterator = new CMSElasticSearchIterator(caseMaintenanceClient, AUTH_TOKEN, TEST_QUERY_BUILDER, TEST_BATCH_SIZE);
    }

    @Test
    public void shouldRetrieveResultsForEmptyResults() {
        when(caseMaintenanceClient.searchCases(eq(AUTH_TOKEN), any())).thenReturn(EMPTY_SEARCH_RESULT);

        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(emptyList()));
        verify(caseMaintenanceClient).searchCases(eq(AUTH_TOKEN), eq(buildCMSBooleanSearchSource(0, TEST_BATCH_SIZE, TEST_QUERY_BUILDER)));
    }

    @Test
    public void shouldRetrieveFirstAndOnlyIncompleteCaseDetailsBatch() {
        when(caseMaintenanceClient.searchCases(AUTH_TOKEN, buildCMSBooleanSearchSource(0, TEST_BATCH_SIZE, TEST_QUERY_BUILDER))).thenReturn(
            SearchResult.builder().cases(List.of(
                CaseDetails.builder().caseId("1").build()
            )).total(1).build()
        );

        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(List.of(CaseDetails.builder().caseId("1").build())));
        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(emptyList()));
        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(emptyList()));
    }

    @Test
    public void shouldRetrieveFirstAndOnlyFullCaseDetailsBatch() {
        when(caseMaintenanceClient.searchCases(AUTH_TOKEN, buildCMSBooleanSearchSource(0, TEST_BATCH_SIZE, TEST_QUERY_BUILDER))).thenReturn(
            SearchResult.builder().cases(List.of(
                CaseDetails.builder().caseId("1").build(),
                CaseDetails.builder().caseId("2").build()
            )).total(2).build()
        );

        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(List.of(
            CaseDetails.builder().caseId("1").build(),
            CaseDetails.builder().caseId("2").build()
        )));
        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(emptyList()));
        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(emptyList()));
    }

    @Test
    public void shouldRetrieveMoreThanOneCaseDetailsBatch() {
        when(caseMaintenanceClient.searchCases(AUTH_TOKEN, buildCMSBooleanSearchSource(0, TEST_BATCH_SIZE, TEST_QUERY_BUILDER))).thenReturn(
            SearchResult.builder().cases(List.of(
                CaseDetails.builder().caseId("1").build(),
                CaseDetails.builder().caseId("2").build()
            )).total(4).build()
        );
        when(caseMaintenanceClient.searchCases(AUTH_TOKEN, buildCMSBooleanSearchSource(2, TEST_BATCH_SIZE, TEST_QUERY_BUILDER))).thenReturn(
            SearchResult.builder().cases(List.of(
                CaseDetails.builder().caseId("3").build(),
                CaseDetails.builder().caseId("4").build()
            )).total(4).build()
        );

        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(List.of(
            CaseDetails.builder().caseId("1").build(),
            CaseDetails.builder().caseId("2").build()
        )));
        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(List.of(
            CaseDetails.builder().caseId("3").build(),
            CaseDetails.builder().caseId("4").build()
        )));
        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(emptyList()));
        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(emptyList()));
    }

    @Test
    public void shouldRetrieveMoreThanOneCaseDetailsBatch_WithAnIncompleteBatch() {
        when(caseMaintenanceClient.searchCases(AUTH_TOKEN, buildCMSBooleanSearchSource(0, TEST_BATCH_SIZE, TEST_QUERY_BUILDER))).thenReturn(
            SearchResult.builder().cases(List.of(
                CaseDetails.builder().caseId("1").build(),
                CaseDetails.builder().caseId("2").build()
            )).total(3).build()
        );
        when(caseMaintenanceClient.searchCases(AUTH_TOKEN, buildCMSBooleanSearchSource(2, TEST_BATCH_SIZE, TEST_QUERY_BUILDER))).thenReturn(
            SearchResult.builder().cases(List.of(
                CaseDetails.builder().caseId("3").build()
            )).total(3).build()
        );

        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(List.of(
            CaseDetails.builder().caseId("1").build(),
            CaseDetails.builder().caseId("2").build()
        )));
        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(List.of(CaseDetails.builder().caseId("3").build())));
        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(emptyList()));
        assertThat(cmsElasticSearchIterator.fetchNextBatch(), equalTo(emptyList()));
        assertThat(cmsElasticSearchIterator.getAmountOfCasesRetrieved(), equalTo(3));
    }

}