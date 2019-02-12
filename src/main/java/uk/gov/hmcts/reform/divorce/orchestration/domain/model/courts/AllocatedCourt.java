package uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "Court allocated to handle a case")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AllocatedCourt {

    @ApiModelProperty(value = "Unique identifier for court")
    private String courtId;

    public AllocatedCourt(String courtId) {
        this.courtId = courtId;
    }

}