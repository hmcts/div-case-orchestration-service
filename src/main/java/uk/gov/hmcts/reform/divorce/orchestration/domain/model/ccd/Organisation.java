package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Organisation {

    @JsonProperty("OrganisationID")
    private String organisationID;

    @JsonProperty("OrganisationName")
    private String organisationName;

    @JsonIgnore
    public boolean isPopulated() {
        return !Strings.isNullOrEmpty(organisationID);
    }
}
