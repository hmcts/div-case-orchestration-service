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
public class CoERespondentSolicitorCoverLetter extends CoEBasicCoverLetter {

    @JsonProperty("solicitorReference")
    private String solicitorReference;

    @Builder(builderMethodName = "coERespondentSolicitorCoverLetterBuilder")
    public CoERespondentSolicitorCoverLetter(CtscContactDetails ctscContactDetails,
                                             String caseReference,
                                             String letterDate,
                                             String petitionerFullName,
                                             String respondentFullName,
                                             Addressee addressee,
                                             boolean costClaimGranted,
                                             String hearingDate,
                                             String deadlineToContactCourtBy,
                                             String solicitorReference) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName,
            addressee, costClaimGranted, hearingDate, deadlineToContactCourtBy);
        this.solicitorReference = solicitorReference;
    }
}
