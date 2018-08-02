package uk.gov.hmcts.reform.divorce.orchestration.domian.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "The response to a callback from ccd")
public class CCDCallbackResponse {
    @ApiModelProperty(value = "The entire case data to be returned with updated fields")
    private Map<String, Object> data;
    @ApiModelProperty(value = "Error messages")
    private List<String> errors;
    @ApiModelProperty(value = "Warning messages")
    private List<String> warnings;
}
