package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@Builder
@EqualsAndHashCode
public class CaseDataResponse {
    private String caseId;

    @JsonProperty("courts")//Keeping existing name in JSON property, but using more sensible name for Java object
    private String court;

    private String state;

    private Map<String, Object> data;

}