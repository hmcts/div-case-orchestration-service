package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ListItem {
    @JsonProperty("code")
    private String code;

    @JsonProperty("label")
    private String label;
}
