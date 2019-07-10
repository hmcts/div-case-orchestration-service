package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

@Component
public class SearchSourceFactory {

    public static SearchSourceBuilder buildCMSBooleanSearchSource(int start, int batchSize, QueryBuilder... builders) {

        BoolQueryBuilder query = QueryBuilders.boolQuery();

        for (QueryBuilder queryBuilder : builders) {
            query.filter(queryBuilder);
        }

        return SearchSourceBuilder
            .searchSource()
            .query(query)
            .from(start)
            .size(batchSize);
    }
}
