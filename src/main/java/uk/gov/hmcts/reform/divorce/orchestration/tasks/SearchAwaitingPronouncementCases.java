package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.*;

@Component
public class SearchAwaitingPronouncementCases implements Task<SearchResult> {

    private final CaseMaintenanceClient caseMaintenanceClient;
    private int page = 0;
    private final int PAGE_SIZE = 0;
    private String HEARING_DATE= "data.hearingDate";

    @Autowired
    public SearchAwaitingPronouncementCases(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public SearchResult execute(TaskContext context, SearchResult payload) {
        QueryBuilder stateQuery = QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, AWAITING_PRONOUNCEMENT);
        QueryBuilder hearingDate  = QueryBuilders.existsQuery(HEARING_DATE);

        final QueryBuilder queries = QueryBuilders
                .boolQuery()
                .must(stateQuery)
                .mustNot(hearingDate);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queries);
        sourceBuilder.from(page);
        sourceBuilder.size(PAGE_SIZE);

        String query = sourceBuilder.toString();
        System.out.println(query);
        return caseMaintenanceClient.searchCases(
                String.valueOf(context.getTransientObject(AUTH_TOKEN_JSON_KEY)),
                query
        );

    }

}
