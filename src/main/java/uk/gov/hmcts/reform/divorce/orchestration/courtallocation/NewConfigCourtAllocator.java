package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation.CourtDistributionConfig;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class NewConfigCourtAllocator implements CourtAllocator {

    private final FactSpecificCourtWeightedDistributor factSpecificCourtWeightedDistributor;
    private final GenericCourtWeightedDistributor genericCourtWeightedDistributor;

    public NewConfigCourtAllocator(@Autowired CourtDistributionConfig courtDistributionConfig) {
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