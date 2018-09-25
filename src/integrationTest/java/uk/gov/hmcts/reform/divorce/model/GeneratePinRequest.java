package uk.gov.hmcts.reform.divorce.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class GeneratePinRequest {
    public final String firstName;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public final String lastName;
    public final List<String> roles;
}

