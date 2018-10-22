package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Connections {

    @JsonProperty("A")
    private String connectionA;

    @JsonProperty("B")
    private String connectionB;

    @JsonProperty("C")
    private String connectionC;

    @JsonProperty("D")
    private String connectionD;

    @JsonProperty("E")
    private String connectionE;

    @JsonProperty("F")
    private String connectionF;

    @JsonProperty("G")
    private String connectionG;
}
