package uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class AuthenticateUserResponse {
    private String code;
}
