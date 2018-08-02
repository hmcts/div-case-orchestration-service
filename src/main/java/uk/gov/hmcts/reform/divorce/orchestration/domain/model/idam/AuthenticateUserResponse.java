package uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthenticateUserResponse {
    @JsonProperty("default-url")
    private String defaultUser;
    @JsonProperty("access-token")
    private String accessToken;
}
