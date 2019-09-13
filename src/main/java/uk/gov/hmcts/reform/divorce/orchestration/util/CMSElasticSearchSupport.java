package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.service.SearchSourceFactory;

import java.util.ArrayList;
import java.util.stream.Stream;

@Slf4j
@Component
public class CMSElasticSearchSupport {

    private final CaseMaintenanceClient caseMaintenanceClient;

    @Autowired
    public CMSElasticSearchSupport(CaseMaintenanceClient caseMaintenanceClient) {
        this.caseMaintenanceClient = caseMaintenanceClient;
    }

    public Stream<CaseDetails> searchCMSCases(int start, int pageSize, String authToken, QueryBuilder... builders) {
        SearchResult finalResult = SearchResult.builder()
            .total(0)
            .cases(new ArrayList<>())
            .build();

        int searchTotalNumberOfResults;
        int rollingSearchResultSize = 0;

        do {
            SearchResult currentSearchResult = caseMaintenanceClient.searchCases(
                authToken,
                SearchSourceFactory.buildCMSBooleanSearchSource(start, pageSize, builders)
            );
            searchTotalNumberOfResults = currentSearchResult.getTotal();
            rollingSearchResultSize += currentSearchResult.getCases().size();
            start += pageSize;
            finalResult.setTotal(searchTotalNumberOfResults);
            finalResult.getCases().addAll(currentSearchResult.getCases());
        }
        while (searchTotalNumberOfResults > rollingSearchResultSize);
        log.info("Search found {} cases.", finalResult.getCases().size());

        return finalResult.getCases().stream();
    }

}