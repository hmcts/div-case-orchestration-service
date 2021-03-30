package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class DivorceServiceApplication {

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

    @JsonProperty("LocalCourtAddress")
    private String localCourtAddress;

    @JsonProperty("LocalCourtEmail")
    private String localCourtEmail;

    @JsonProperty("CertificateOfServiceDate")
    private String certificateOfServiceDate;

    @JsonProperty("SuccessfulServedByBailiff")
    private String successfulServedByBailiff;

    @JsonProperty("ReasonFailureToServe")
    private String reasonFailureToServe;
}
