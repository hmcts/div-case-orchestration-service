package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "The response for a citizen submitting or updating a case")
@Builder
public class CaseResponse {
    @ApiModelProperty(value = "The case ID returned on successful request")
    private String caseId;
    @ApiModelProperty(value = "An error message if any for a failed request")
    private String error;
    @ApiModelProperty(value = "The result of the request, whether [success] or [error]")
    private String status;
}
