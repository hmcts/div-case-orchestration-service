package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;

@Data
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class AosOverdueCoverLetter extends BasicCoverLetter {

    @JsonProperty("hasHelpWithFeesNumber")
    private boolean hasHelpWithFeesNumber = false;

    @JsonProperty("helpWithFeesNumber")
    private String helpWithFeesNumber;

    @Builder(builderMethodName = "aosOverdueCoverLetterBuilder")
    public AosOverdueCoverLetter(CtscContactDetails ctscContactDetails,
                                 String caseReference,
                                 String letterDate,
                                 String petitionerFullName,
                                 String respondentFullName,
                                 Addressee addressee,
                                 String helpWithFeesNumber) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName, addressee);

        if (StringUtils.isNotEmpty(helpWithFeesNumber)) {
            this.helpWithFeesNumber = helpWithFeesNumber;
            this.hasHelpWithFeesNumber = true;
        }
    }

}