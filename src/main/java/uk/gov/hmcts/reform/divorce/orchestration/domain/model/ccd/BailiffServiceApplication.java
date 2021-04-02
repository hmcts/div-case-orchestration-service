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
        String applicationGranted,
        String decisionDate,
        String payment,
        String refusalReason,
        String localCourtDetailsLabel,
        String localCourtAddress,
        String localCourtEmail,
        String bailiffReturnLabel,
        String certificateOfServiceDate,
        String successfulServedByBailiff,
        String reasonFailureToServe) {
        super(addedDate, receivedDate, type, applicationGranted, decisionDate, payment, refusalReason);

        this.localCourtDetailsLabel = localCourtDetailsLabel;
        this.localCourtAddress = localCourtAddress;
        this.localCourtEmail = localCourtEmail;
        this.bailiffReturnLabel = bailiffReturnLabel;
        this.certificateOfServiceDate = certificateOfServiceDate;
        this.successfulServedByBailiff = successfulServedByBailiff;
        this.reasonFailureToServe = reasonFailureToServe;
    }

    @JsonProperty("LocalCourtDetailsLabel")
    private String localCourtDetailsLabel;

    @JsonProperty("LocalCourtAddress")
    private String localCourtAddress;

    @JsonProperty("LocalCourtEmail")
    private String localCourtEmail;

    @JsonProperty("BailiffReturnLabel")
    private String bailiffReturnLabel;

    @JsonProperty("CertificateOfServiceDate")
    private String certificateOfServiceDate;

    @JsonProperty("SuccessfulServedByBailiff")
    private String successfulServedByBailiff;

    @JsonProperty("ReasonFailureToServe")
    private String reasonFailureToServe;
}
