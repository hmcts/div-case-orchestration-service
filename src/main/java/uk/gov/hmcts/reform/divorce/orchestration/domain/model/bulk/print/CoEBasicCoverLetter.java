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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.BasicCoverLetter;

@Data
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class CoEBasicCoverLetter extends BasicCoverLetter {

    @JsonProperty("costClaimGranted")
    private boolean costClaimGranted;

    @JsonProperty("hearingDate")
    private String hearingDate;

    @JsonProperty("deadlineToContactCourtBy")
    private String deadlineToContactCourtBy;

    @Builder(builderMethodName = "coEBasicCoverLetterBuilder")
    public CoEBasicCoverLetter(CtscContactDetails ctscContactDetails,
                               String caseReference,
                               String letterDate,
                               String petitionerFullName,
                               String respondentFullName,
                               Addressee addressee,
                               boolean costClaimGranted,
                               String hearingDate,
                               String deadlineToContactCourtBy) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName, addressee);
        this.costClaimGranted = costClaimGranted;
        this.hearingDate = hearingDate;
        this.deadlineToContactCourtBy = deadlineToContactCourtBy;
    }

}
