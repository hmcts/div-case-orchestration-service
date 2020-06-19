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
public class CoECoverLetter extends CoEBasicCoverLetter {

    @JsonProperty("courtName")
    private String courtName;

    @Builder(builderMethodName = "coECoverLetterBuilder")
    public CoECoverLetter(CtscContactDetails ctscContactDetails,
                          String caseReference,
                          String letterDate,
                          String petitionerFullName,
                          String respondentFullName,
                          Addressee addressee,
                          boolean costClaimGranted,
                          String hearingDate,
                          String deadlineToContactCourtBy,
                          String courtName) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName,
            addressee, costClaimGranted, hearingDate, deadlineToContactCourtBy);
        this.courtName = courtName;
    }
}