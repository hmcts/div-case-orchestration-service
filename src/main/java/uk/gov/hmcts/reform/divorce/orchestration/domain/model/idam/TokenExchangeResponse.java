package uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class TokenExchangeResponse {
    @JsonProperty("access_token")
    private String accessToken;
}
