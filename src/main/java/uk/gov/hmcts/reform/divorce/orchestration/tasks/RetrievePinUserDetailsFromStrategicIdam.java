package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.StrategicIdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.AuthenticationError;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LOCATION_HEADER;

@Component
public class RetrievePinUserDetailsFromStrategicIdam extends RetrievePinUserDetails {

    private final StrategicIdamClient idamClient;

    @Autowired
    public RetrievePinUserDetailsFromStrategicIdam(StrategicIdamClient idamClient) {
        this.idamClient = idamClient;
    }

    @Override
    protected String authenticatePinUser(String pin, String authClientId, String authRedirectUrl) throws TaskException {
        Response authenticateResponse = idamClient.authenticatePinUser(pin, authClientId, authRedirectUrl);

        if (authenticateResponse.status() == HttpStatus.OK.value()
            || authenticateResponse.status() == HttpStatus.FOUND.value()) {
            return getCodeFromRedirect(authenticateResponse);
        }

        throw new TaskException(new AuthenticationError(String.format("Error authenticating RESPONDENT_PIN [%s]", pin)));
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
