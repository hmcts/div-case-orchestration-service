package uk.gov.hmcts.reform.divorce.orchestration.domain.pdf;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Map;

@Value
@ApiModel(description = "Request body model for Document Generation Request")
public class PdfGenerateDocumentRequest {
    @ApiModelProperty(value = "Name of the template", required = true)
    @JsonProperty(value = "template", required = true)
    @NotBlank
    private final String template;
    @JsonProperty(value = "values", required = true)
    @ApiModelProperty(value = "Placeholder key / value pairs", required = true)
    private final Map<String, Object> values;
}