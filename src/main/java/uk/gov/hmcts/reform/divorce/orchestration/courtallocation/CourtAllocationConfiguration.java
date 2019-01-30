package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode
public class CourtAllocationConfiguration {

    private List<CourtWeight> courtsWeightedDistribution = new ArrayList<>();
    private List<CourtAllocationPerReason> courtsForSpecificReasons = new ArrayList<>();

}