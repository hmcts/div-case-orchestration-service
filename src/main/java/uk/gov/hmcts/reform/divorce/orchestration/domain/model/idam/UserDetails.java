package uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class UserDetails {

    private final String id;
    private final String email;

    private final String forename;
    private final String surname;
    private String authToken;
    private final List<String> roles;
}
