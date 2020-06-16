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
public class CoRespondentCostOrderNotificationCoverLetter extends DocmosisTemplateVars {
    @JsonProperty("addressee")
    private Addressee addressee;
    @JsonProperty("hearingDate")
    private String hearingDate;
    @JsonProperty("solicitorReference")
    private String solicitorReference;

    @Builder
    public CoRespondentCostOrderNotificationCoverLetter(
        CtscContactDetails ctscContactDetails,
        String caseReference,
        String solicitorReference,
        String letterDate,
        String hearingDate,
        String petitionerFullName,
        String respondentFullName,
        Addressee addressee) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName);
        this.addressee = addressee;
        this.hearingDate = hearingDate;
        this.solicitorReference = solicitorReference;
    }
}
