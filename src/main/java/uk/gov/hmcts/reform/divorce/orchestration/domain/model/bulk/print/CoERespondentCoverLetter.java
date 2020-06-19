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
public class CoERespondentCoverLetter extends CoECoverLetter {

    @JsonProperty("husbandOrWife")
    private String husbandOrWife;

    @Builder(builderMethodName = "coERespondentCoverLetterBuilder")
    public CoERespondentCoverLetter(CtscContactDetails ctscContactDetails,
                                    String caseReference,
                                    String letterDate,
                                    String petitionerFullName,
                                    String respondentFullName,
                                    Addressee addressee,
                                    boolean costClaimGranted,
                                    String hearingDate,
                                    String deadlineToContactCourtBy,
                                    String courtName,
                                    String husbandOrWife) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName, addressee,
            costClaimGranted, hearingDate, deadlineToContactCourtBy, courtName);
        this.husbandOrWife = husbandOrWife;
    }

}
