package uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RejectReasonAddress {

    @JsonProperty("RejectReasonAddressType")
    private String rejectReasonAddressType;

    @JsonProperty("RejectReasonAddressText")
    private String rejectReasonAddressText;

}
