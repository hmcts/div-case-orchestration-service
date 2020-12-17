package uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CARE_OF_PREFIX;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMAIL_LABEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINE_SEPARATOR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PHONE_LABEL;

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
        String careOfCourt = String.join(SPACE, CARE_OF_PREFIX, name);
        String emailContact = String.join(SPACE, EMAIL_LABEL, email);
        String phoneContact = String.join(SPACE, PHONE_LABEL, phone);
        return String.join(LINE_SEPARATOR, careOfCourt, address, EMPTY_STRING, emailContact, phoneContact);
    }
}