package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchForCase {

    private final CaseMaintenanceClient caseMaintenanceClient;
    private final AuthUtil authUtil;

    public Optional<List<CaseDetails>> search(String searchTerm, String searchValue) {
        log.info("Search for case in CCD  with search term: {}", searchTerm);
        String searchString =
            buildQuery(searchValue, searchTerm);
        List<CaseDetails> caseDetails = caseMaintenanceClient.searchCases(
            authUtil.getCaseworkerToken(),
            searchString).getCases();
        if (caseDetails == null || caseDetails.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(caseDetails);
    }

    private String buildQuery(String searchValue, String searchField) {
        String searchString = "{\"query\":{\"term\":{ \""
            + searchField
            + "\":\"" + searchValue + "\"}}}";
        return searchString;
    }
}
