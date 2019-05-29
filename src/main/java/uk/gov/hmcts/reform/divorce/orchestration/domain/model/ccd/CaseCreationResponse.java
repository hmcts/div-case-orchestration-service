package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;

@ApiModel(description = "The response to a citizen submitting a case")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CaseCreationResponse {

    @ApiModelProperty(value = "The case ID returned on successful request")
    private String caseId;

    @ApiModelProperty(value = "An error message if any for a failed request")
    private String error;

    @ApiModelProperty(value = "The result of the request, whether [success] or [error]")
    private String status;

    @ApiModelProperty(value = "The court that was allocated to handle the case")
    private Court allocatedCourt;

}