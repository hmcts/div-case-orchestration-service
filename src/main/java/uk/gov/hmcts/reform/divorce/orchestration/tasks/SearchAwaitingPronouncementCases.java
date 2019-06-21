package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;

@Component
public class SearchAwaitingPronouncementCases implements Task<Map<String, Object>> {

    private static final String HEARING_DATE = "data.DateAndTimeOfHearing";
    private static final String BULK_LISTING_CASE_ID = "data.BulkListingCaseId";

    @Value("${bulk-action.page-size:50}")
    private int pageSize;

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public SearchAwaitingPronouncementCases(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {

        List<SearchResult> searchResultList = new ArrayList<>();
        int from = 0;
        int totalSearch;
        do {
            QueryBuilder stateQuery = QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, AWAITING_PRONOUNCEMENT);
            QueryBuilder hearingDate = QueryBuilders.existsQuery(HEARING_DATE);
            QueryBuilder bulkListingCaseId = QueryBuilders.existsQuery(BULK_LISTING_CASE_ID);

            final QueryBuilder query = QueryBuilders
                .boolQuery()
                .must(stateQuery)
                .mustNot(hearingDate)
                .mustNot(bulkListingCaseId);

            SearchSourceBuilder sourceBuilder =  SearchSourceBuilder
                .searchSource()
                .query(query)
                .from(from)
                .size(pageSize);

            SearchResult result = caseMaintenanceClient.searchCases(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                sourceBuilder.toString()
            );

            from  += result.getCases().size();
            totalSearch = result.getTotal();
            if (!result.getCases().isEmpty()) {
                searchResultList.add(result);
            }
        }
        while (from < totalSearch);
        context.setTransientObject(SEARCH_RESULT_KEY, searchResultList);

        return payload;

    }

}
