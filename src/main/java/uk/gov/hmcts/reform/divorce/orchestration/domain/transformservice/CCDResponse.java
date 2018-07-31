package uk.gov.hmcts.reform.divorce.orchestration.domain.transformservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "The response of a document submission to CCD")
public class CCDResponse {
    @ApiModelProperty(value = "The id of the case as returned from CCD.", example = "1234567890123456")
    private long caseId;
    @ApiModelProperty(value = "If the status is not \"success\" this field will contain explanation of the problem.", example = "null")
    private String error;
    @ApiModelProperty(value = "The result of the request.", allowableValues = "success, error", example = "success")
    private String status;
}
