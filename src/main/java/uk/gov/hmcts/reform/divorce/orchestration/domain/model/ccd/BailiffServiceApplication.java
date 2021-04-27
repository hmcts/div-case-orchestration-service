package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class BailiffServiceApplication extends DivorceServiceApplication {

    @Builder(builderMethodName = "bailiffServiceApplicationBuilder")
    public BailiffServiceApplication(
        String addedDate,
        String receivedDate,
        String type,
        String bailiffApplicationGranted,
        String applicationGranted,
        String decisionDate,
        String payment,
        String refusalReason,
        String localCourtAddress,
        String localCourtEmail,
        String certificateOfServiceDate,
        String successfulServedByBailiff,
        String reasonFailureToServe) {
        super(addedDate, receivedDate, type, applicationGranted, decisionDate, payment, refusalReason);

        this.bailiffApplicationGranted = bailiffApplicationGranted;
        this.localCourtAddress = localCourtAddress;
        this.localCourtEmail = localCourtEmail;
        this.certificateOfServiceDate = certificateOfServiceDate;
        this.successfulServedByBailiff = successfulServedByBailiff;
        this.reasonFailureToServe = reasonFailureToServe;
    }

    @JsonProperty("BailiffApplicationGranted")
    private String bailiffApplicationGranted;

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
