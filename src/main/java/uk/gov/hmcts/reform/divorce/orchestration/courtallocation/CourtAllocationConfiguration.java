package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class CourtAllocationConfiguration {

    private Set<CourtWeight> courtsWeightedDistribution = new HashSet<>();
    private Set<CourtAllocationPerReason> courtsForSpecificReasons = new HashSet<>();

}