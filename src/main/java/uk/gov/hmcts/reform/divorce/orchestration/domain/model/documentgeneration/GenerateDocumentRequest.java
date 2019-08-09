package uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@ApiModel(description = "Request body model for Document Generation Request")
/**
 * This is representation of a request for generating a document as per the DGS domain.
 */
public class GenerateDocumentRequest {
    @ApiModelProperty(value = "Name of the template", required = true)
    @JsonProperty(value = "template", required = true)
    @NotBlank
    private final String template;
    @JsonProperty(value = "values", required = true)
    @ApiModelProperty(value = "Placeholder key / value pairs", required = true)
    private final Map<String, Object> values;
}
