package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SearchSourceFactoryTest {

    private int pageSize;
    private int start;
    private String queryKey = "testKey";
    private String queryValue = "AwaitingDA";

    @Test
    public void buildCMSBooleanSearchSource_givenOneQuery() {
        pageSize = 10;
        start = 0;
        QueryBuilder firstQuery = QueryBuilders.matchQuery(queryKey, queryValue);

        QueryBuilder testQuery = QueryBuilders
            .boolQuery()
            .filter(firstQuery);

        SearchSourceBuilder expectedSearchSource = SearchSourceBuilder
            .searchSource()
            .query(testQuery)
            .from(start)
            .size(pageSize);

        SearchSourceBuilder actualSearchSource = SearchSourceFactory
            .buildCMSBooleanSearchSource(0, 10, firstQuery);

        assertEquals(expectedSearchSource, actualSearchSource);
    }

    @Test
    public void buildCMSBooleanSearchSource_givenThreeQueries() {
        pageSize = 5;
        start = 0;
        QueryBuilder firstQuery = QueryBuilders.matchQuery(queryKey, queryValue);
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

        SearchSourceBuilder actualSearchSource = SearchSourceFactory
            .buildCMSBooleanSearchSource(0, 5, firstQuery, secondQuery, thirdQuery);

        assertEquals(expectedSearchSource, actualSearchSource);
    }
}
