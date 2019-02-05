package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class CandidateCourtAllocator implements CourtAllocator {

    private final FactSpecificCourtWeightedDistributor factSpecificCourtWeightedDistributor;
    private final GenericCourtWeightedDistributor genericCourtWeightedDistributor;

    public CandidateCourtAllocator(Map<String, BigDecimal> desiredWorkloadPerCourt,
                                   Map<String, BigDecimal> divorceRatioPerFact,
                                   Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {
        this.factSpecificCourtWeightedDistributor = new FactSpecificCourtWeightedDistributor(specificCourtsAllocationPerFact);
        this.genericCourtWeightedDistributor = new GenericCourtWeightedDistributor(desiredWorkloadPerCourt, divorceRatioPerFact, specificCourtsAllocationPerFact);
    }

    @Override
    public String selectCourtForGivenDivorceFact(Optional<String> divorceFact) {//TODO - happy using Optionals but maybe the parameter should just be a string - since I always expect it, but don't need it to make a decision
        return divorceFact.map(factSpecificCourtWeightedDistributor::selectCourt)
            .orElseGet(genericCourtWeightedDistributor::selectCourt);
    }

}