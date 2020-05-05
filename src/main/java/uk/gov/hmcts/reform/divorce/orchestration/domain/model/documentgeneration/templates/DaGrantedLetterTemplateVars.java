package uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.templates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Address;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class DaGrantedLetterTemplateVars {
    @JsonProperty("RespondentFullName")
    private String respondentFullName;

    @JsonProperty("PetitionerFullName")
    private String petitionerFullName;

    @JsonProperty("Address")
    private Address address;

    @JsonProperty("CaseReference")
    private String caseReference;

    @JsonProperty("DecreeAbsoluteDate")
    private String decreeAbsoluteDate;
}