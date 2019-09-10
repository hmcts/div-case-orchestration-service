package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.SearchSourceFactory;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.transformation.CaseDetailsMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class CMSHelper {

    private final CaseDetailsMapper caseDetailsMapper;
    private CaseMaintenanceClient caseMaintenanceClient;

    public CMSHelper(CaseMaintenanceClient caseMaintenanceClient, CaseDetailsMapper caseDetailsMapper) {
        this.caseMaintenanceClient = caseMaintenanceClient;
        this.caseDetailsMapper = caseDetailsMapper;
    }

    public List<String> searchCMSCases(int start, int pageSize, String authToken, QueryBuilder... builders) throws TaskException {
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

        List<String> transformedResults = new ArrayList<>();
        for (CaseDetails caseDetails : finalResult.getCases()) {
            Optional<String> transformedCaseData = caseDetailsMapper.mapCaseData(caseDetails);
            transformedCaseData.ifPresent(transformedResults::add);
        }
        return transformedResults;
    }

}