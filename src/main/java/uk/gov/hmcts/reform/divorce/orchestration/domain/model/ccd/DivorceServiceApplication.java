package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class DivorceServiceApplication {

    @Builder
    public DivorceServiceApplication(
        String addedDate,
        String receivedDate,
        String type,
        String applicationGranted,
        String decisionDate,
        String payment,
        String refusalReason) {

        this.addedDate = addedDate;
        this.receivedDate = receivedDate;
        this.type = type;
        this.applicationGranted = applicationGranted;
        this.decisionDate = decisionDate;
        this.payment = payment;
        this.refusalReason = refusalReason;
    }

    @JsonProperty("ReceivedDate")
    private String receivedDate;

    @JsonProperty("AddedDate")
    private String addedDate;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Payment")
    private String payment;

    @JsonProperty("ApplicationGranted")
    private String applicationGranted;

    @JsonProperty("DecisionDate")
    private String decisionDate;

    @JsonProperty("RefusalReason")
    private String refusalReason;
}
