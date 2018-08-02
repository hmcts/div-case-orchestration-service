package uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "The validation request")
public class ValidationRequest {
    @ApiModelProperty(value = "The data to be validated")
    private Map<String, Object> data;
    @ApiModelProperty(value = "The form the data came from")
    private String formId;
    @ApiModelProperty(value = "The section of the form to be validated")
    private String sectionId;
}