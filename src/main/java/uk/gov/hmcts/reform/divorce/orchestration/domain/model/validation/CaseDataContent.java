package uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class CaseDataContent {
    private Event event;
    private Map<String, JsonNode> data;
    @JsonProperty("security_classification")
    private Map<String, JsonNode> securityClassification;
    @JsonProperty("event_token")
    private String token;
    @JsonProperty("ignore_warning")
    private Boolean ignoreWarning;

}
