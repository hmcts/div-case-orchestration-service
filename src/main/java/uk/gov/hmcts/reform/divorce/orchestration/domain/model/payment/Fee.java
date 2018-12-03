
package uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "calculated_amount",
    "ccd_case_number",
    "code",
    "memo_line",
    "natural_account_code",
    "reference",
    "version",
    "volume"
    })
public class Fee {

    @JsonProperty("calculated_amount")
    private Integer calculatedAmount;
    @JsonProperty("ccd_case_number")
    private String ccdCaseNumber;
    @JsonProperty("code")
    private String code;
    @JsonProperty("memo_line")
    private String memoLine;
    @JsonProperty("natural_account_code")
    private String naturalAccountCode;
    @JsonProperty("reference")
    private String reference;
    @JsonProperty("version")
    private String version;
    @JsonProperty("volume")
    private Integer volume;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("calculated_amount")
    public Integer getCalculatedAmount() {
        return calculatedAmount;
    }

    @JsonProperty("calculated_amount")
    public void setCalculatedAmount(Integer calculatedAmount) {
        this.calculatedAmount = calculatedAmount;
    }

    @JsonProperty("ccd_case_number")
    public String getCcdCaseNumber() {
        return ccdCaseNumber;
    }

    @JsonProperty("ccd_case_number")
    public void setCcdCaseNumber(String ccdCaseNumber) {
        this.ccdCaseNumber = ccdCaseNumber;
    }

    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("memo_line")
    public String getMemoLine() {
        return memoLine;
    }

    @JsonProperty("memo_line")
    public void setMemoLine(String memoLine) {
        this.memoLine = memoLine;
    }

    @JsonProperty("natural_account_code")
    public String getNaturalAccountCode() {
        return naturalAccountCode;
    }

    @JsonProperty("natural_account_code")
    public void setNaturalAccountCode(String naturalAccountCode) {
        this.naturalAccountCode = naturalAccountCode;
    }

    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @JsonProperty("reference")
    public void setReference(String reference) {
        this.reference = reference;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("volume")
    public Integer getVolume() {
        return volume;
    }

    @JsonProperty("volume")
    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
