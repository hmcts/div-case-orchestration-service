package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class CourtAllocationConfiguration {

    //TODO - delete these fields and subclasses
    private Set<CourtWeight> courtsWeightedDistribution = new HashSet<>();
    private Set<CourtAllocationPerReason> courtsForSpecificReasons = new HashSet<>();

    private Map<String, BigDecimal> desiredWorkloadPerCourt;
    private Map<String, BigDecimal> divorceRatioPerFact;
    private Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact;

    public CourtAllocationConfiguration(Map<String, BigDecimal> desiredWorkloadPerCourt, Map<String, BigDecimal> divorceRatioPerFact, Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {
        this.desiredWorkloadPerCourt = desiredWorkloadPerCourt;
        this.divorceRatioPerFact = divorceRatioPerFact;
        this.specificCourtsAllocationPerFact = specificCourtsAllocationPerFact;
    }
}