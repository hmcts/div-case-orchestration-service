package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
public class CourtAllocationConfiguration {

    private Map<String, BigDecimal> desiredWorkloadPerCourt;
    private Map<String, BigDecimal> divorceRatioPerFact;
    private Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact;

    public CourtAllocationConfiguration(Map<String, BigDecimal> desiredWorkloadPerCourt,
                                        Map<String, BigDecimal> divorceRatioPerFact,
                                        Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {
        this.desiredWorkloadPerCourt = desiredWorkloadPerCourt;
        this.divorceRatioPerFact = divorceRatioPerFact;
        this.specificCourtsAllocationPerFact = specificCourtsAllocationPerFact;
    }

}