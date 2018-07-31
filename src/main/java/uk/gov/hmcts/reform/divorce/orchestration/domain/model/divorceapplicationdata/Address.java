package uk.gov.hmcts.reform.divorce.orchestration.domain.model.divorceapplicationdata;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
public @Data class Address {
    @ApiModelProperty("Address type.")
    private String addressType;
    @ApiModelProperty(value = "Address post code.")
    private String postcode;
    @JsonProperty("address")
    @ApiModelProperty(value = "Address.")
    private List<String> addressField;
    @ApiModelProperty(value = "Street line 1.")
    private String street1;
    @ApiModelProperty(value = "Street line 2.")
    private String street2;
    @ApiModelProperty(value = "Town.")
    private String town;
    @ApiModelProperty(value = "Postcode manual.")
    private String postcodeManual;
    @ApiModelProperty(value = "Is the address confirmed?")
    private boolean addressConfirmed;
    @ApiModelProperty(value = "Address abroad.")
    private String addressAbroad;
    @ApiModelProperty(value = "Is the postcode valid?")
    private boolean validPostcode;
    @ApiModelProperty(value = "Is there an error in the postcode?")
    private boolean postcodeError;
    @ApiModelProperty(value = "URL.")
    private String url;
}