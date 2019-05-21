package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AosCaseData {
    @JsonProperty("RespConfirmReadPetition")
    private String respConfirmReadPetition;

    @JsonProperty("RespJurisdictionAgree")
    private String respJurisdictionAgree;

    @JsonProperty("RespAdmitOrConsentToFact")
    private String respAdmitOrConsentToFact;

    @JsonProperty("RespWillDefendDivorce")
    private String respWillDefendDivorce;

    @JsonProperty("RespJurisdictionDisagreeReason")
    private String respJurisdictionDisagreeReason;

    @JsonProperty("RespJurisdictionRespCountryOfResidence")
    private String respJurisdictionRespCountryOfResidence;

    @JsonProperty("RespLegalProceedingsExist")
    private String respLegalProceedingsExist;

    @JsonProperty("RespLegalProceedingsDescription")
    private String respLegalProceedingsDescription;

    @JsonProperty("RespAgreeToCosts")
    private String respAgreeToCosts;

    @JsonProperty("RespCostsAmount")
    private String respCostsAmount;

    @JsonProperty("RespCostsReason")
    private String respCostsReason;

    @JsonProperty("RespEmailAddress")
    private String respEmailAddress;

    @JsonProperty("RespPhoneNumber")
    private String respPhoneNumber;

    @JsonProperty("RespStatementOfTruth")
    private String respStatementOfTruth;

    @JsonProperty("RespConsentToEmail")
    private String respConsentToEmail;

    @JsonProperty("RespConsiderFinancialSituation")
    private String respConsiderFinancialSituation;

    @JsonProperty("RespHardshipDefenseResponse")
    private String respHardshipDefenseResponse;

    @JsonProperty("RespHardshipDescription")
    private String respHardshipDescription;

    @JsonProperty("RespContactMethodIsDigital")
    private String respContactMethodIsDigital;

    @JsonProperty("dueDate")
    private String dueDate;
}
