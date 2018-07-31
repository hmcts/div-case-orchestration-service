package uk.gov.hmcts.reform.divorce.orchestration.domain.model.divorceapplicationdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@ApiModel("Divorce event which requires the divorce session to be resubmitted")
public class DivorceEventSession {
    @ApiModelProperty("The updated divorce session")
    private DivorceSession eventData;
    @ApiModelProperty("The id of the event")
    private String eventId;
}