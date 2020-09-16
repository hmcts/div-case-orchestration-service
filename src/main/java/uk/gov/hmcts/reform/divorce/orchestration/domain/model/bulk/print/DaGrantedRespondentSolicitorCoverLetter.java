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
public class DaGrantedRespondentSolicitorCoverLetter extends BasicCoverLetter {

    @JsonProperty("recipientReference")
    private String recipientReference;

    @Builder(builderMethodName = "daGrantedRespondentSolicitorCoverLetterBuilder")
    public DaGrantedRespondentSolicitorCoverLetter(CtscContactDetails ctscContactDetails,
                                                   String caseReference,
                                                   String letterDate,
                                                   String petitionerFullName,
                                                   String respondentFullName,
                                                   Addressee addressee,
                                                   String recipientReference) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName, addressee);
        this.recipientReference = recipientReference;
    }

}