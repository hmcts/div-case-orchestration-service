package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
<<<<<<< HEAD
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;

@Data
@Builder
@EqualsAndHashCode(callSuper=false)
=======
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;

@Data
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
>>>>>>> 7f6ad23fd0b1ed8e947eb6dbf667ccf9dc811c22
public class DaGrantedLetter extends DocmosisTemplateVars {
    @JsonProperty("respondentFullName")
    private String respondentFullName;
    @JsonProperty("petitionerFullName")
    private String petitionerFullName;
    @JsonProperty("addressee")
    private Addressee addressee;
    @JsonProperty("decreeAbsoluteDate")
    private String decreeAbsoluteDate;
<<<<<<< HEAD
=======

    @Builder
    public DaGrantedLetter(
        CtscContactDetails ctscContactDetails,
        String caseReference,
        String respondentFullName,
        String petitionerFullName,
        Addressee addressee,
        String decreeAbsoluteDate) {
        super(ctscContactDetails, caseReference);
        this.respondentFullName = respondentFullName;
        this.petitionerFullName = petitionerFullName;
        this.addressee = addressee;
        this.decreeAbsoluteDate = decreeAbsoluteDate;
    }
>>>>>>> 7f6ad23fd0b1ed8e947eb6dbf667ccf9dc811c22
}
