package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class DefaultCourtAllocator implements CourtAllocator {

    private final FactSpecificCourtWeightedDistributor factSpecificCourtWeightedDistributor;
    private final GenericCourtWeightedDistributor genericCourtWeightedDistributor;

    public DefaultCourtAllocator(Map<String, BigDecimal> desiredWorkloadPerCourt,
                                 Map<String, BigDecimal> divorceRatioPerFact,
                                 Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {
        this.factSpecificCourtWeightedDistributor =
            new FactSpecificCourtWeightedDistributor(specificCourtsAllocationPerFact);
        this.genericCourtWeightedDistributor = new GenericCourtWeightedDistributor(desiredWorkloadPerCourt,
            divorceRatioPerFact, specificCourtsAllocationPerFact);
    }

    @Override
    public String selectCourtForGivenDivorceFact(Optional<String> fact) {
        return fact.map(factSpecificCourtWeightedDistributor::selectCourt)
            .orElseGet(genericCourtWeightedDistributor::selectCourt);
    }

}