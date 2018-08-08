package uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "The response to a validation request")
@Builder
public class ValidationResponse {
    @ApiModelProperty(value = "The status of the validation")
    private String validationStatus;
    @ApiModelProperty(value = "The warnings returned by the validation")
    private List<String> warnings;
    @ApiModelProperty(value = "The errors returned by the validation")
    private List<String> errors;

    @JsonIgnore
    public boolean isValid() {
        boolean hasNoErrors = this.getErrors() == null || this.getErrors().isEmpty();
        boolean hasNoWarnings = this.getWarnings() == null || this.getWarnings().isEmpty();

        return hasNoErrors && hasNoWarnings;
    }
}