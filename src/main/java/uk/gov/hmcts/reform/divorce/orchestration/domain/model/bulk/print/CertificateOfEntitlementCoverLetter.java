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
public class CertificateOfEntitlementCoverLetter extends DocmosisTemplateVars {
    @JsonProperty("hearingDate")
    private String hearingDate;

    @JsonProperty("husbandOrWife")
    private String husbandOrWife;

    @JsonProperty("courtName")
    private String courtName;

    @JsonProperty("deadlineToContactCourtBy")
    private String deadlineToContactCourtBy;

    @JsonProperty("costClaimGranted")
    private boolean costClaimGranted;

    @JsonProperty("addressee")
    private Addressee addressee;

    @JsonProperty("solicitorReference")
    private String solicitorReference;

    @Builder
    public CertificateOfEntitlementCoverLetter(
        CtscContactDetails ctscContactDetails,
        String caseReference,
        String letterDate,
        String petitionerFullName,
        String respondentFullName,
        String hearingDate,
        String husbandOrWife,
        String courtName,
        String deadlineToContactCourtBy,
        String solicitorReference,
        boolean costClaimGranted,
        Addressee addressee) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName);
        this.hearingDate = hearingDate;
        this.husbandOrWife = husbandOrWife;
        this.courtName = courtName;
        this.deadlineToContactCourtBy = deadlineToContactCourtBy;
        this.costClaimGranted = costClaimGranted;
        this.addressee = addressee;
        this.solicitorReference = solicitorReference;
    }
}
