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
public class ServiceDecisionOrder extends DocmosisTemplateVars {

    @JsonProperty("documentIssuedOn")
    private String documentIssuedOn;

    @JsonProperty("receivedServiceApplicationDate")
    private String receivedServiceApplicationDate;

    @JsonProperty("serviceApplicationDecisionDate")
    private String serviceApplicationDecisionDate;

    @Builder(builderMethodName = "serviceDecisionOrderBuilder")
    public ServiceDecisionOrder(
        CtscContactDetails ctscContactDetails,
        String caseReference,
        String letterDate,
        String petitionerFullName,
        String respondentFullName,
        String documentIssuedOn,
        String receivedServiceApplicationDate,
        String serviceApplicationDecisionDate) {
        super(ctscContactDetails, caseReference, letterDate, petitionerFullName, respondentFullName);
        this.documentIssuedOn = documentIssuedOn;
        this.receivedServiceApplicationDate = receivedServiceApplicationDate;
        this.serviceApplicationDecisionDate = serviceApplicationDecisionDate;
    }
}
