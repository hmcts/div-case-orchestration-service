package uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Links {

    @JsonProperty("cancel")
    private Url cancel;

    @JsonProperty("next_url")
    private Url nextUrl;

    @JsonProperty("self")
    private Url self;

}
