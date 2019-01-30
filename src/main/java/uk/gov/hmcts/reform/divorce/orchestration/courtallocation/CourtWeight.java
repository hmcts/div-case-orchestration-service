package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Court weight uses positive integer weight as the court weight as opposed to decimal percentual representation.
 * <p/>
 * For example: to configure two courts, one of them being twice as likely to be chosen,
 * just configure one to have weight "1" and the other to have weight "2".
 */
@Getter
@EqualsAndHashCode
public class CourtWeight {

    private final String courtId;
    private final int weight;

    @JsonCreator
    public CourtWeight(@JsonProperty("courtId") String courtId,
                       @JsonProperty("weight") int weight) {
        this.courtId = courtId;
        this.weight = weight;
    }

}