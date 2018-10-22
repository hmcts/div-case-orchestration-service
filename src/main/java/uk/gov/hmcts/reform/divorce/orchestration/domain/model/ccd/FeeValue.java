package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
public class FeeValue {

    @JsonProperty("FeeDescription")
    private String feeDescription;

    @JsonProperty("FeeVersion")
    private String feeVersion;

    @JsonProperty("FeeCode")
    private String feeCode;

    @JsonProperty("FeeAmount")
    private String feeAmount;
}
