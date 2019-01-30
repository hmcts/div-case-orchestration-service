package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class CourtAllocationPerReason {

    private String courtId;
    private String divorceReason;

    @JsonCreator
    public CourtAllocationPerReason(@JsonProperty("courtId") String courtId,
                                    @JsonProperty("reason") String divorceReason) {
        this.courtId = courtId;
        this.divorceReason = divorceReason;
    }

}