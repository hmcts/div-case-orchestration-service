package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.StrategicIdamClient;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LOCATION_HEADER;

@Component
@ConditionalOnProperty(value = "idam.strategic.enabled", havingValue = "true")
public class RetrievePinUserDetailsFromStrategicIdam extends RetrievePinUserDetails {

    private final StrategicIdamClient idamClient;

    @Autowired
    public RetrievePinUserDetailsFromStrategicIdam(@Qualifier("strategicIdamClient") StrategicIdamClient idamClient) {
        this.idamClient = idamClient;
    }

    @Override
    protected String authenticatePinUser(String pin, String authClientId, String authRedirectUrl) {
        return getCodeFromRedirect(idamClient.authenticatePinUser(pin, authClientId, authRedirectUrl));
    }

    @Override
    protected IdamClient getIdamClient() {
        return idamClient;
    }

    private String getCodeFromRedirect(Response response) {
        String location = response.headers().get(LOCATION_HEADER).stream().findFirst()
            .orElseThrow(IllegalArgumentException::new);

        UriComponents build = UriComponentsBuilder.fromUriString(location).build();
        return build.getQueryParams().getFirst(CODE);
    }
}
