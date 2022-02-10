package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class SearchForCaseByEmail {

    private final CaseMaintenanceClient caseMaintenanceClient;
    private final AuthUtil authUtil;

    String buildQuery(String searchValue, String searchField) {
        String searchString = "{\"query\":{\"term\":{ \""
            + searchField
            + ".keyword\":\"" + searchValue + "\"}}}";
        return searchString;
    }

    @Autowired
    public SearchForCaseByEmail(CaseMaintenanceClient caseMaintenanceClient, AuthUtil authUtil) {
        this.caseMaintenanceClient = caseMaintenanceClient;
        this.authUtil = authUtil;
    }

    public Optional<List<CaseDetails>> searchCasesByEmail(String emailAddress) {
        log.info("Search for case in CCD for Citizen, emailAddress: {}", emailAddress);
        String searchString =
            buildQuery(emailAddress, "data.D8PetitionerEmail");
        List<CaseDetails> caseDetails = caseMaintenanceClient.searchCases(
            authUtil.getCaseworkerToken(),
            searchString).getCases();
        if (caseDetails == null || caseDetails.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(caseDetails);
    }

}
