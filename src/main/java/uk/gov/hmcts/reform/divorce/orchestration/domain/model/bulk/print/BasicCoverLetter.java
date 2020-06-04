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
public class BasicCoverLetter extends DocmosisTemplateVars {
    @JsonProperty("addressee")
    private Addressee addressee;

    @Builder
    public BasicCoverLetter(
        CtscContactDetails ctscContactDetails,
        String caseReference,
        String letterDate,
        String petitionerFullName,
        String respondentFullName,
        Addressee addressee) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName);
        this.addressee = addressee;
    }
}
