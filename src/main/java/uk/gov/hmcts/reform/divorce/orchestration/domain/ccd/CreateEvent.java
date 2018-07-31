package uk.gov.hmcts.reform.divorce.orchestration.domain.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateEvent {
    
    private String token;
    
    @JsonProperty("event_id")
    private String eventId;
    
    @JsonProperty("case_details")
    private CaseDetails caseDetails;
}
