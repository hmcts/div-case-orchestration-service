package uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CMSElasticSearchSupport {

    public static final String ELASTIC_SEARCH_DAYS_REPRESENTATION = "d";
    private static final Function<CaseDetails, Optional<CaseDetails>> NO_OP_CASE_DETAILS_TRANSFORMATION_FUNCTION = Optional::ofNullable;

    private final CaseMaintenanceClient caseMaintenanceClient;

    public Stream<CaseDetails> searchCMSCases(String authToken, QueryBuilder... filters) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        for (QueryBuilder filter : filters) {
            queryBuilder = queryBuilder.filter(filter);
        }

        Stream.Builder<CaseDetails> foundCases = Stream.builder();
        searchTransformAndProcessCMSElasticSearchCases(createNewCMSElasticSearchIterator(authToken, queryBuilder),
            NO_OP_CASE_DETAILS_TRANSFORMATION_FUNCTION,
            foundCases::add);
        return foundCases.build();
    }

    public static <T> void searchTransformAndProcessCMSElasticSearchCases(CMSElasticSearchIterator cmsElasticSearchIterator,
                                                                          Function<CaseDetails, Optional<T>> caseDetailsTransformationFunction,
                                                                          Consumer<? super T> postTransformationOperation) {

        List<CaseDetails> caseDetails;
        do {
            caseDetails = cmsElasticSearchIterator.fetchNextBatch();

            caseDetails.stream()
                .map(caseDetailsTransformationFunction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(postTransformationOperation);
        }
        while (!caseDetails.isEmpty());

        log.info("Search found {} cases.", cmsElasticSearchIterator.getAmountOfCasesRetrieved());
    }

    public static String buildDateForTodayMinusGivenPeriod(final String durationExp) {
        String timeUnit = String.valueOf(durationExp.charAt(durationExp.length() - 1));
        return String.format("now/%s-%s", timeUnit, durationExp);
    }

    public static String buildCMSBooleanSearchSource(int start, int batchSize, QueryBuilder query) {
        return SearchSourceBuilder
            .searchSource()
            .query(query)
            .from(start)
            .size(batchSize)
            .toString();
    }

    public CMSElasticSearchIterator createNewCMSElasticSearchIterator(String authToken, QueryBuilder queryBuilder) {
        return new CMSElasticSearchIterator(caseMaintenanceClient, authToken, queryBuilder);
    }

}