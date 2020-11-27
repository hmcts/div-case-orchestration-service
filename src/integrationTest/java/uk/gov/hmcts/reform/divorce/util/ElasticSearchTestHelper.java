package uk.gov.hmcts.reform.divorce.util;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.support.cms.CmsClientSupport;

import java.util.List;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.pollinterval.FibonacciPollInterval.fibonacci;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ES_CASE_ID_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchSupport.buildCMSBooleanSearchSource;

@Component
public class ElasticSearchTestHelper {

    private CmsClientSupport cmsClientSupport;

    public ElasticSearchTestHelper(CmsClientSupport cmsClientSupport) {
        this.cmsClientSupport = cmsClientSupport;
    }

    public void ensureCaseIsSearchable(final String caseId, final String authToken, String expectedState) {
        await().pollInterval(fibonacci(SECONDS)).atMost(120, SECONDS).untilAsserted(() -> {
            List<CaseDetails> foundCases = searchCasesWithElasticSearch(caseId, authToken, expectedState);
            assertThat("The number of cases found by ElasticSearch was not expected", foundCases, hasSize(1));
        });
    }

    private List<CaseDetails> searchCasesWithElasticSearch(final String caseId, final String authToken, String expectedState) {
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
            .filter(QueryBuilders.matchQuery(ES_CASE_ID_KEY, caseId))
            .filter(QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, expectedState));
        String searchSourceBuilder = buildCMSBooleanSearchSource(0, 10, queryBuilder);

        return cmsClientSupport.searchCases(searchSourceBuilder, authToken);
    }

}
