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
public class CoECoverLetter extends DocmosisTemplateVars {
    @JsonProperty("addressee")
    private Addressee addressee;
    @JsonProperty("costClaimGranted")
    private boolean costClaimGranted;
    @JsonProperty("courtName")
    private String courtName;
    @JsonProperty("hearingDate")
    private String hearingDate;
    @JsonProperty("deadlineToContactCourtBy")
    private String deadlineToContactCourtBy;

    @Builder
    public CoECoverLetter(
        CtscContactDetails ctscContactDetails,
        String caseReference,
        String letterDate,
        String petitionerFullName,
        String respondentFullName,
        Addressee addressee,
        boolean costClaimGranted,
        String courtName,
        String hearingDate,
        String deadlineToContactCourtBy) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName);
        this.addressee = addressee;
        this.costClaimGranted = costClaimGranted;
        this.courtName = courtName;
        this.hearingDate = hearingDate;
        this.deadlineToContactCourtBy = deadlineToContactCourtBy;
    }
}
