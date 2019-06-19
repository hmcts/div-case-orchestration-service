package uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINE_SEPARATOR;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DnCourt {

    @JsonProperty("name")
    private String name;

    @JsonProperty("address")
    private String address;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    private String formattedContactDetails;

    @JsonIgnore
    public String getFormattedContactDetails() {
        if (formattedContactDetails == null) {
            formattedContactDetails = formatContactDetails();
        }

        return formattedContactDetails;
    }

    private String formatContactDetails() {
        return String.join(LINE_SEPARATOR, address, email, phone);
    }
}