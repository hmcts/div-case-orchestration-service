package uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch;

import org.elasticsearch.index.query.QueryBuilder;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;

import java.util.List;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchSupport.buildCMSBooleanSearchSource;

public class CMSElasticSearchIterator {

    private static final int DEFAULT_SEARCH_PAGE_SIZE = 50;

    private final CaseMaintenanceClient caseMaintenanceClient;
    private final String authToken;
    private final QueryBuilder queryBuilder;
    private final int pageSize;

    private int accumulatedNumberOfSearchedCases = 0;
    private int startSearchIndexForNext = 0;
    private int totalNumberOfCases;

    public CMSElasticSearchIterator(CaseMaintenanceClient caseMaintenanceClient, String authToken, QueryBuilder queryBuilder, int pageSize) {
        this.caseMaintenanceClient = caseMaintenanceClient;
        this.pageSize = pageSize;
        this.authToken = authToken;
        this.queryBuilder = queryBuilder;
    }

    public CMSElasticSearchIterator(CaseMaintenanceClient caseMaintenanceClient, String authToken, QueryBuilder queryBuilder) {
        this(caseMaintenanceClient, authToken, queryBuilder, DEFAULT_SEARCH_PAGE_SIZE);
    }

    /**
     * Returns next <code>CaseDetails</code> batch as a list or an empty list if there are no more <code>CaseDetails</code> to fetch.
     */
    public List<CaseDetails> fetchNextBatch() {
        List<CaseDetails> batchToReturn;

        if (moreResultsExist()) {
            SearchResult currentSearchResult = caseMaintenanceClient.searchCases(
                authToken,
                buildCMSBooleanSearchSource(startSearchIndexForNext, pageSize, queryBuilder)
            );

            List<CaseDetails> currentSearchResultCases = currentSearchResult.getCases();
            accumulatedNumberOfSearchedCases += currentSearchResultCases.size();
            totalNumberOfCases = currentSearchResult.getTotal();

            startSearchIndexForNext += pageSize;

            batchToReturn = currentSearchResultCases;
        } else {
            batchToReturn = emptyList();
        }

        return batchToReturn;
    }

    private boolean moreResultsExist() {
        return totalNumberOfCases > accumulatedNumberOfSearchedCases || searchWasNotInitiated();
    }

    private boolean searchWasNotInitiated() {
        return startSearchIndexForNext == 0;
    }

    public int getAmountOfCasesRetrieved() {
        return accumulatedNumberOfSearchedCases;
    }

}