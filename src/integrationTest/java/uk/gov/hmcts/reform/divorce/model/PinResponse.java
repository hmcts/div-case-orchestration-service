package uk.gov.hmcts.reform.divorce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PinResponse {
    private String pin;
    private String userId;
}
