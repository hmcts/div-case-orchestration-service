package uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pin {
    private final String pin;
    private final String expiryDate;
    private final String userId;
}
