package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class DivorceGeneralReferral {

    @JsonProperty("GeneralReferralReason")
    private String generalReferralReason;

    @JsonProperty("GeneralApplicationFrom")
    private String generalApplicationFrom;

    @JsonProperty("GeneralApplicationReferralDate")
    private String generalApplicationReferralDate;

    @JsonProperty("GeneralApplicationAddedDate")
    private String generalApplicationAddedDate;

    @JsonProperty("GeneralReferralType")
    private String generalReferralType;

    @JsonProperty("AlternativeServiceMedium")
    private String alternativeServiceMedium;

    @JsonProperty("GeneralReferralDetails")
    private String generalReferralDetails;

    @JsonProperty("GeneralReferralFee")
    private String generalReferralFee;

    @JsonProperty("GeneralReferralPaymentType")
    private String generalReferralPaymentType;

    @JsonProperty("GeneralReferralDecisionDate")
    private String generalReferralDecisionDate;

    @JsonProperty("GeneralReferralDecision")
    private String generalReferralDecision;

    @JsonProperty("GeneralReferralDecisionReason")
    private String generalReferralDecisionReason;
}
