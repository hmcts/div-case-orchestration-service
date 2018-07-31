package uk.gov.hmcts.reform.divorce.orchestration.domain.transformservice;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CoreCaseData;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "The response to a feedback call by ccd")
public class CCDCallbackResponse {
    @ApiModelProperty(value = "The entire case data to be returned with updated fields")
    private CoreCaseData data;
    @ApiModelProperty(value = "Some error messages")
    private List<String> errors;
    @ApiModelProperty(value = "Some warning messages")
    private List<String> warnings;
}
