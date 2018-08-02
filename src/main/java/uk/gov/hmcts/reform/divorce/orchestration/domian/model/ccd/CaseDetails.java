package uk.gov.hmcts.reform.divorce.orchestration.domian.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseDetails {

    @JsonProperty("case_data")
    private Map<String, Object> caseData;

    @JsonProperty("id")
    private String caseId;

    @JsonProperty("state")
    private String state;
}
