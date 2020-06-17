package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;

@Data
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class CoRespondentCostOrderCoverLetter extends DocmosisTemplateVars {
    @JsonProperty("addressee")
    private Addressee addressee;
    @JsonProperty("hearingDate")
    private String hearingDate;

    @Builder
    public CoRespondentCostOrderCoverLetter(
        CtscContactDetails ctscContactDetails,
        String caseReference,
        String letterDate,
        String hearingDate,
        String petitionerFullName,
        String respondentFullName,
        Addressee addressee) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName);
        this.addressee = addressee;
        this.hearingDate = hearingDate;
    }
}
