package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.TacticalIdamClient;

import java.util.Base64;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN_PREFIX;

@Component
@ConditionalOnProperty(value = "idam.strategic.enabled", havingValue = "false")
public class RetrievePinUserDetailsFromTacticalIdam extends RetrievePinUserDetails {
    private final TacticalIdamClient idamClient;

    @Autowired
    public RetrievePinUserDetailsFromTacticalIdam(@Qualifier("tacticalIdamClient") TacticalIdamClient idamClient) {
        this.idamClient = idamClient;
    }

    @Override
    protected String authenticatePinUser(String pin, String authClientId, String authRedirectUrl) {
        String authorisation = PIN_PREFIX + new String(Base64.getEncoder().encode(pin.getBytes()));

        return idamClient.authenticatePinUser(authorisation, CODE, authClientId, authRedirectUrl).getCode();
    }

    @Override
    protected IdamClient getIdamClient() {
        return idamClient;
    }
}
