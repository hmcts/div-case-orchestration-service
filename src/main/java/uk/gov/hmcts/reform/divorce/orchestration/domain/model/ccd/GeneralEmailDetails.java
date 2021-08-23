package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class GeneralEmailDetails {

    @JsonProperty("GeneralEmailDateTime")
    private String generalEmailDateTime;

    @JsonProperty("GeneralEmailParties")
    private String generalEmailParties;

    @JsonProperty("GeneralEmailOtherRecipientEmail")
    private String generalEmailOtherRecipientEmail;

    @JsonProperty("GeneralEmailOtherRecipientName")
    private String generalEmailOtherRecipientName;

    @JsonProperty("GeneralEmailCreatedBy")
    private String generalEmailCreatedBy;

    @JsonProperty("GeneralEmailBody")
    private String generalEmailBody;
}
