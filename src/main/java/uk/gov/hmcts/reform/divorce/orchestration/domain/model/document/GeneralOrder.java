package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;

@Data
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class GeneralOrder extends DocmosisTemplateVars {

    @JsonProperty("hasCoRespondent")
    private boolean hasCoRespondent;

    @JsonProperty("coRespondentFullName")
    private String coRespondentFullName;

    @JsonProperty("generalOrderRecitals")
    private String generalOrderRecitals;

    @JsonProperty("judgeType")
    private String judgeType;

    @JsonProperty("judgeName")
    private String judgeName;

    @JsonProperty("generalOrderDate")
    private String generalOrderDate;

    @JsonProperty("generalOrderDetails")
    private String generalOrderDetails;

    @Builder(builderMethodName = "generalOrderBuilder")
    public GeneralOrder(
        CtscContactDetails ctscContactDetails,
        String caseReference,
        String letterDate,
        String petitionerFullName,
        String respondentFullName,
        boolean hasCoRespondent,
        String coRespondentFullName,
        String generalOrderRecitals,
        String judgeType,
        String judgeName,
        String generalOrderDate,
        String generalOrderDetails) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName);

        this.hasCoRespondent = hasCoRespondent;
        this.coRespondentFullName = coRespondentFullName;
        this.generalOrderRecitals = generalOrderRecitals;
        this.judgeType = judgeType;
        this.judgeName = judgeName;
        this.generalOrderDate = generalOrderDate;
        this.generalOrderDetails = generalOrderDetails;
    }
}
