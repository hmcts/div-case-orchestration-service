package uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditAccountPaymentRequest {

    @JsonProperty("ccd_case_number")
    private String ccdCaseNumber;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("case_reference")
    private String caseReference;

    @JsonProperty("fees")
    private List<PaymentItem> fees;

    @JsonProperty("service")
    private String service;

    @JsonProperty("customer_reference")
    private String customerReference;

    @JsonProperty("site_id")
    private String siteId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("organisation_name")
    private String organisationName;

    public void setAmount(String amount) {
        String value = Optional.ofNullable(amount)
                .map(Double::parseDouble).map(i -> i / 100)
                .map(String::valueOf).orElse(amount);
        this.amount = value;
    }

}
