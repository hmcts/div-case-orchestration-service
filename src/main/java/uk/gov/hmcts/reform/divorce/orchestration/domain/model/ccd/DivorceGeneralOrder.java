package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DivorceGeneralOrder {

    @JsonProperty("Document")
    private Document document;

    @JsonProperty("GeneralOrderParties")
    private List<GeneralOrderParty> generalOrderParties;
}
