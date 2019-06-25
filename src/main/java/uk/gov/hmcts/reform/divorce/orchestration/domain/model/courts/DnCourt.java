package uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMAIL_LABEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINE_SEPARATOR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PHONE_LABEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SPACE_SEPARATOR;

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
        String emailContact = String.join(SPACE_SEPARATOR, EMAIL_LABEL, email);
        String phoneContact = String.join(SPACE_SEPARATOR, PHONE_LABEL, phone);
        return String.join(LINE_SEPARATOR, address, EMPTY_STRING, emailContact, phoneContact);
    }
}