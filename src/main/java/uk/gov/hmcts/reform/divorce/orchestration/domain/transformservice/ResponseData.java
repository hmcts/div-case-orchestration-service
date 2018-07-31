package uk.gov.hmcts.reform.divorce.orchestration.domain.transformservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseData {
    @JsonProperty("caseId")
    private long caseId;

    @JsonProperty("status")
    private String status;
}
