package uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.Optional;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentItem {

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("volume")
    private String volume;

    @JsonProperty("ccd_case_number")
    private String ccdCaseNumber;

    @JsonProperty("memo_line")
    private String memoLine;

    @JsonProperty("natural_account_code")
    private String naturalAccountCode;

    @JsonProperty("code")
    private String code;

    @JsonProperty("calculated_amount")
    private String calculatedAmount;

    @JsonProperty("version")
    private String version;

    public void setCalculatedAmount(String calculatedAmount) {
        this.calculatedAmount = Optional.ofNullable(calculatedAmount)
                .map(Double::parseDouble)
                .map(i -> i / 100).map(String::valueOf)
                .orElse(calculatedAmount);
    }

}