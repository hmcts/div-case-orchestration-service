package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

public @Data class RejectReason {
    
    @JsonProperty("RejectReasonType")
    private String rejectReasonType;

    @JsonProperty("RejectReasonText")
    private String rejectReasonText;

}
