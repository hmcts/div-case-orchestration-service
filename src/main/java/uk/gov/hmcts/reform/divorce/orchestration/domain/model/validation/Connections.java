package uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Connections {
    @JsonProperty("A")
    private String a;

    @JsonProperty("B")
    private String b;

    @JsonProperty("C")
    private String c;

    @JsonProperty("D")
    private String d;

    @JsonProperty("E")
    private String e;

    @JsonProperty("F")
    private String f;

    @JsonProperty("G")
    private String g;
}