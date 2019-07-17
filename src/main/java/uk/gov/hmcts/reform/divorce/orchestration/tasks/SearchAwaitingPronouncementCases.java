package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.Setter;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_DECISION_DATE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_OUTCOME_FLAG_CCD_FIELD;

@Component
public class SearchAwaitingPronouncementCases implements Task<Map<String, Object>> {

    private static final String HEARING_DATE = String.format("data.%s", DATETIME_OF_HEARING_CCD_FIELD);
    private static final String BULK_LISTING_CASE_ID = String.format("data.%s",BULK_LISTING_CASE_ID_FIELD);
    private static final String IS_DN_OUTCOME_CASE = String.format("data.%s", DN_OUTCOME_FLAG_CCD_FIELD);
    private static final String DN_DECISION_DATE_DATA_FIELD = String.format("data.%s", DN_DECISION_DATE_FIELD);

    @Value("${bulk-action.page-size:50}")
    @Setter
    private int pageSize;

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public SearchAwaitingPronouncementCases(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {

        List<SearchResult> searchResultList = new ArrayList<>();
        Set<String> processedCaseIds = new HashSet<>();

        int from = 0;
        int totalSearch;
        do {
            QueryBuilder stateQuery = QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, AWAITING_PRONOUNCEMENT);
            QueryBuilder hearingDate = QueryBuilders.existsQuery(HEARING_DATE);
            QueryBuilder bulkListingCaseId = QueryBuilders.existsQuery(BULK_LISTING_CASE_ID);
            QueryBuilder checkByLaField = QueryBuilders.existsQuery(IS_DN_OUTCOME_CASE);

            QueryBuilder query = QueryBuilders
                .boolQuery()
                .must(stateQuery)
                .must(checkByLaField)
                .mustNot(hearingDate)
                .mustNot(bulkListingCaseId);

            SearchSourceBuilder sourceBuilder =  SearchSourceBuilder
                .searchSource()
                .sort(DN_DECISION_DATE_DATA_FIELD, SortOrder.ASC)
                .query(query)
                .from(from)
                .size(pageSize);

            SearchResult result = caseMaintenanceClient.searchCases(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                sourceBuilder.toString()
            );

            from += pageSize;
            totalSearch = result.getTotal();

            result.setCases(result.getCases().stream()
                .filter(caseDetails -> !processedCaseIds.contains(caseDetails.getCaseId()))
                .collect(Collectors.toList()));

            result.getCases().stream()
                    .forEach(caseDetails -> processedCaseIds.add(caseDetails.getCaseId()));

            if (!result.getCases().isEmpty()) {
                searchResultList.add(result);
            }
        }
        while (from < totalSearch);
        context.setTransientObject(SEARCH_RESULT_KEY, searchResultList);

        return payload;

    }

}