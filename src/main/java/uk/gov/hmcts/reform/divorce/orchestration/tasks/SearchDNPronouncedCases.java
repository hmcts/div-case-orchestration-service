package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.SearchSourceFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DA_PERIOD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_PRONOUNCED;

@Component
@Slf4j
public class SearchDNPronouncedCases implements Task<Map<String, Object>> {

    private static String DN_GRANTED_DATE = String.format("data.%s", DECREE_NISI_GRANTED_DATE_CCD_FIELD);

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public SearchDNPronouncedCases(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        int start = context.<Integer>getTransientObjectOptional("FROM").orElse(0);
        int pageSize = context.<Integer>getTransientObjectOptional("PAGE_SIZE").orElse(50);
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String coolOffPeriodInDN = context.getTransientObject(AWAITING_DA_PERIOD_KEY);

        QueryBuilder stateQuery = QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, DN_PRONOUNCED);
        QueryBuilder dateFilter = QueryBuilders.rangeQuery(DN_GRANTED_DATE).lte(buildCoolOffPeriodInDNBoundary(coolOffPeriodInDN));

        try {
            SearchResult finalResult = searchDNPronouncedCasesEligibleForDA(start, pageSize, authToken, stateQuery, dateFilter);
            context.setTransientObject(SEARCH_RESULT_KEY, returnCaseIdsOnly(finalResult));
        } catch (FeignException fException) {
            log.error("DN Pronounced cases eligible for DA search job failed: " + fException.getMessage(), fException);
            throw fException;
        }

        return payload;
    }

    private String buildCoolOffPeriodInDNBoundary(final String coolOffPeriodInDN) {
        String timeUnit = String.valueOf(coolOffPeriodInDN.charAt(coolOffPeriodInDN.length() - 1));
        return String.format("now/%s-%s", timeUnit, coolOffPeriodInDN );
    }

    private SearchResult searchDNPronouncedCasesEligibleForDA(int start, int pageSize, String authToken, QueryBuilder... builders) {
        SearchResult finalResult = SearchResult.builder()
            .total(0)
            .cases(new ArrayList<>())
            .build();

        int searchTotalNumberOfResults;
        int rollingSearchResultSize = 0;

        do {
            SearchResult currentSearchResult = caseMaintenanceClient.searchCases(
                authToken,
                SearchSourceFactory
                    .buildCMSBooleanSearchSource(start, pageSize, builders)
                    .toString()
            );
            searchTotalNumberOfResults = currentSearchResult.getTotal();
            rollingSearchResultSize += currentSearchResult.getCases().size();
            start += pageSize;
            finalResult.setTotal(searchTotalNumberOfResults);
            finalResult.getCases().addAll(currentSearchResult.getCases());
        }
        while (searchTotalNumberOfResults > rollingSearchResultSize);
        log.info("Search found {} cases.",  finalResult.getCases().size());
        return finalResult;
    }

    private List<String> returnCaseIdsOnly(SearchResult finalResult) {
        return finalResult.getCases()
            .stream()
            .map(CaseDetails::getCaseId)
            .collect(Collectors.toList());
    }
}
