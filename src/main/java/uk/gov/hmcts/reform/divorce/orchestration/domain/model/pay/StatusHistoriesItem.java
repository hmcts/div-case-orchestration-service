package uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class StatusHistoriesItem {

    @JsonProperty("date_updated")
    private String dateUpdated;

    @JsonProperty("date_created")
    private String dateCreated;

    @JsonProperty("external_status")
    private String externalStatus;

    @JsonProperty("status")
    private String status;

}
