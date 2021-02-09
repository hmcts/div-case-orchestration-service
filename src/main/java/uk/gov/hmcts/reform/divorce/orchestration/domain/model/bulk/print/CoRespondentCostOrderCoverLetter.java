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
public class CoRespondentCostOrderCoverLetter extends BasicCoverLetter {

    @JsonProperty("hearingDate")
    private String hearingDate;

    @Builder(builderMethodName = "coRespondentCostOrderCoverLetterBuilder")
    public CoRespondentCostOrderCoverLetter(CtscContactDetails ctscContactDetails,
                                            String caseReference,
                                            String letterDate,
                                            String petitionerFullName,
                                            String respondentFullName,
                                            Addressee addressee,
                                            String hearingDate) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName, addressee);
        this.hearingDate = hearingDate;
    }

}
