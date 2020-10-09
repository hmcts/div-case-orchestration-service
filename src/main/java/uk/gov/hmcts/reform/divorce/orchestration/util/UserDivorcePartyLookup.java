package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.parties.DivorceParty;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.PETITIONER;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;

@RequiredArgsConstructor
@Component
public class UserDivorcePartyLookup {

    private final IdamClient idamClient;

    public Optional<DivorceParty> lookupDivorcePartForGivenUser(String authToken, Map<String, Object> caseDataInCcdFormat) {
        UserDetails userDetails = idamClient.getUserDetails(authToken);

        return findDivorcePartyForUser(caseDataInCcdFormat, userDetails);
    }

    private Optional<DivorceParty> findDivorcePartyForUser(Map<String, Object> caseDataInCcdFormat, UserDetails userDetails) {
        String emailAddress = userDetails.getEmail();

        DivorceParty divorceParty = null;

        if (Objects.equals(caseDataInCcdFormat.get(D_8_PETITIONER_EMAIL), emailAddress)) {
            divorceParty = PETITIONER;
        } else if (Objects.equals(caseDataInCcdFormat.get(RESPONDENT_EMAIL_ADDRESS), emailAddress)) {
            divorceParty = RESPONDENT;
        } else if (Objects.equals(caseDataInCcdFormat.get(CO_RESP_EMAIL_ADDRESS), emailAddress)) {
            divorceParty = CO_RESPONDENT;
        }

        return Optional.ofNullable(divorceParty);
    }

}