package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;

public class CertificateOfService extends DocmosisTemplateVars {

    @JsonProperty("hasCoRespondent")
    private boolean hasCoRespondent;

    @JsonProperty("coRespondentFullName")
    private String coRespondentFullName;

    @Builder(builderMethodName = "certificateOfServiceBuilder")
    public CertificateOfService(CtscContactDetails ctscContactDetails,
                                String caseReference,
                                String petitionerFullName,
                                String respondentFullName,
                                boolean hasCoRespondent,
                                String coRespondentFullName) {
        super(ctscContactDetails, caseReference, null, petitionerFullName, respondentFullName);
        this.hasCoRespondent = hasCoRespondent;
        this.coRespondentFullName = coRespondentFullName;
    }
}
