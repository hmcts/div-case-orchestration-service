package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation.CourtDistributionConfig;

@Component
public class CourtConfigAllocator implements CourtAllocator {

    private final FactSpecificCourtWeightedDistributor factSpecificCourtWeightedDistributor;
    private final GenericCourtWeightedDistributor genericCourtWeightedDistributor;

    @Autowired
    public CourtConfigAllocator(CourtDistributionConfig courtDistributionConfig) {
        this.factSpecificCourtWeightedDistributor =
            new FactSpecificCourtWeightedDistributor(courtDistributionConfig.getFactAllocation());
        this.genericCourtWeightedDistributor = new GenericCourtWeightedDistributor(
            courtDistributionConfig.getDistribution(),
            courtDistributionConfig.getDivorceCasesRatio(),
            courtDistributionConfig.getFactAllocation()
        );
    }

    @Override
    public String selectCourtForGivenDivorceFact(String fact) {
        return factSpecificCourtWeightedDistributor.selectCourt(fact)
            .orElseGet(genericCourtWeightedDistributor::selectCourt);
    }

}