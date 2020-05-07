package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print;

import com.fasterxml.jackson.annotation.JsonProperty;
<<<<<<< HEAD
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
=======
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
>>>>>>> 7f6ad23fd0b1ed8e947eb6dbf667ccf9dc811c22
}