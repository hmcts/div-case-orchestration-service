package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class CaseLink {
    @JsonProperty("CaseReference")
    private String caseReference;
}
