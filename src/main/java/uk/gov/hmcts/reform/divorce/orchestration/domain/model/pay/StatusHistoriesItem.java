package uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusHistoriesItem {

    @JsonProperty("date_updated")
    private String dateUpdated;

    @JsonProperty("date_created")
    private String dateCreated;

    @JsonProperty("external_status")
    private String externalStatus;

    @JsonProperty("status")
    private String status;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;
}
