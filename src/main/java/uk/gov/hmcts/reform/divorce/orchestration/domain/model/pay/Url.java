package uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Url {

    @JsonProperty("method")
    private String method;

    @JsonProperty("href")
    private String href;

}