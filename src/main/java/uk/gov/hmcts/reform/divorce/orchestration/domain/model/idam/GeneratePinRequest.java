package uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

import java.util.List;

@Value
public class GeneratePinRequest {
    public final String firstName;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public final String lastName;
    public final List<String> roles;
}

