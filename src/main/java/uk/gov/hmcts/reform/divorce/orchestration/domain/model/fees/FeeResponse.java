package uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.text.DecimalFormat;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "The response from retrieving a fee from fees and payments service")
@Builder
public class FeeResponse {
    @ApiModelProperty(value = "The fee identifier")
    private String feeCode;
    @ApiModelProperty(value = "The fee amount in pounds")
    private Double amount;
    @ApiModelProperty(value = "The fee version")
    private Integer version;
    @ApiModelProperty(value = "The fee description")
    private String description;

    public String getFormattedFeeAmount() {
        DecimalFormat df = new DecimalFormat("0.00");
        // Remove trailing pence if 0
        return df.format(amount).replace(".00", "");
    }
}
