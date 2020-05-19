package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;

@Data
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DaGrantedLetter extends DocmosisTemplateVars {
    @JsonProperty("respondentFullName")
    private String respondentFullName;
    @JsonProperty("petitionerFullName")
    private String petitionerFullName;
    @JsonProperty("addressee")
    private Addressee addressee;

    @Builder
    public DaGrantedLetter(
        CtscContactDetails ctscContactDetails,
        String caseReference,
        String letterDate,
        String respondentFullName,
        String petitionerFullName,
        Addressee addressee) {
        super(ctscContactDetails, caseReference, letterDate);
        this.respondentFullName = respondentFullName;
        this.petitionerFullName = petitionerFullName;
        this.addressee = addressee;
    }
}
