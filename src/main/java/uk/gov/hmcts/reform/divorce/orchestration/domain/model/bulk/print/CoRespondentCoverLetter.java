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
public class CoRespondentCoverLetter extends BasicCoverLetter {
    @JsonProperty("CoRespondentFullName")
    private String coRespondentFullName;

    @Builder(builderMethodName = "builder1")
    public CoRespondentCoverLetter(
        CtscContactDetails ctscContactDetails,
        String caseReference,
        String letterDate,
        String petitionerFullName,
        String respondentFullName,
        Addressee addressee,
        String coRespondentFullName) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName, addressee);
        this.coRespondentFullName = coRespondentFullName;
    }
}
