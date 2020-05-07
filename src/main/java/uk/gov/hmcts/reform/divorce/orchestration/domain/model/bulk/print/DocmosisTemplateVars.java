package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocmosisTemplateVars {
    @JsonProperty("ctscContactDetails")
    protected CtscContactDetails ctscContactDetails;
    @JsonProperty("caseReference")
    protected String caseReference;
}