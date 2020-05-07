package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;

@Data
@Builder
@EqualsAndHashCode(callSuper=false)
public class DaGrantedLetter extends DocmosisTemplateVars {
    @JsonProperty("respondentFullName")
    private String respondentFullName;
    @JsonProperty("petitionerFullName")
    private String petitionerFullName;
    @JsonProperty("addressee")
    private Addressee addressee;
    @JsonProperty("decreeAbsoluteDate")
    private String decreeAbsoluteDate;
}
