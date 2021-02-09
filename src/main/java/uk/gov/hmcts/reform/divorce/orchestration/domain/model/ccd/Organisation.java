package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class Organisation {

    @JsonProperty("OrganisationID")
    private String organisationID;

    @JsonProperty("OrganisationName")
    private String organisationName;
}
