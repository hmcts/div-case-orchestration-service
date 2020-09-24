package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;

import java.util.ArrayList;
import java.util.stream.Stream;

@Slf4j
@Component
public class CMSElasticSearchSupport {

    public static final String ELASTIC_SEARCH_DAYS_REPRESENTATION = "d";

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public CMSElasticSearchSupport(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    public Stream<CaseDetails> searchCMSCases(String authToken, QueryBuilder... builders) {
        SearchResult finalResult = SearchResult.builder()
            .total(0)
            .cases(new ArrayList<>())
            .build();

        int searchTotalNumberOfResults;
        int rollingSearchResultSize = 0;
        int upperSearchLimit;
        int start = 0;
        int pageSize = 50;

        do {
            SearchResult currentSearchResult = caseMaintenanceClient.searchCases(
                authToken,
                buildCMSBooleanSearchSource(start, pageSize, builders)
            );
            searchTotalNumberOfResults = currentSearchResult.getTotal();
            upperSearchLimit = searchTotalNumberOfResults;
            rollingSearchResultSize += currentSearchResult.getCases().size();
            start += pageSize;
            finalResult.setTotal(searchTotalNumberOfResults);
            finalResult.getCases().addAll(currentSearchResult.getCases());
        }
        while (upperSearchLimit > rollingSearchResultSize);
        log.info("Search found {} cases.", finalResult.getCases().size());

        return finalResult.getCases().stream();
    }

    public static String buildDateForTodayMinusGivenPeriod(final String durationExp) {
        String timeUnit = String.valueOf(durationExp.charAt(durationExp.length() - 1));
        return String.format("now/%s-%s", timeUnit, durationExp);
    }

    public static String buildCMSBooleanSearchSource(int start, int batchSize, QueryBuilder... builders) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        for (QueryBuilder queryBuilder : builders) {
            query.filter(queryBuilder);
        }

        return SearchSourceBuilder
            .searchSource()
            .query(query)
            .from(start)
            .size(batchSize)
            .toString();
    }

}