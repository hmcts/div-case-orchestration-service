package uk.gov.hmcts.reform.divorce.orchestration.domain.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CoreCaseData;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseEvent {
    @JsonProperty("id")
    private long caseId;

    @JsonProperty("case_data")
    private CoreCaseData caseData;

    @JsonProperty("security_classification")
    private String securityClassification;

    @JsonProperty("state")
    private String state;
}