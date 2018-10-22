package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FeeItem {

    @JsonProperty("id")
    private String id;

    @JsonProperty("value")
    private FeeValue value;

}
