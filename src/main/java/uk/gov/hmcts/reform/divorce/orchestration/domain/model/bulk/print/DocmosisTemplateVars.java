package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;

@Data
@Builder
@EqualsAndHashCode
public class DocmosisTemplateVars {
    @JsonProperty("ctscContactDetails")
    private CtscContactDetails ctscContactDetails;
    @JsonProperty("caseReference")
    private String caseReference;
}