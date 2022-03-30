package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SearchForCaseByEmail {

    private final SearchForCase searchForCase;

    public Optional<List<CaseDetails>> searchCasesByEmail(String emailAddress) {
        log.info("Search for case in CCD for Citizen, emailAddress: {}", emailAddress);
        return searchForCase.search("data.D8PetitionerEmail", emailAddress);
    }
}
