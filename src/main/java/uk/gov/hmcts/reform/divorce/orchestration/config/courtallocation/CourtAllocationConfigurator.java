package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationConfiguration;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.DefaultCourtAllocator;

import java.math.BigDecimal;
import java.util.Map;

public class CourtAllocationConfigurator {

    @Autowired
    private CourtDistributionConfig courtDistributionConfig;

    @Bean
    public CourtAllocator configureCourtAllocationFromEnvironmentVariable() {
        return new DefaultCourtAllocator(
            courtDistributionConfig.getDistribution(),
            courtDistributionConfig.getDivorceCasesRatio(),
            courtDistributionConfig.getFactAllocation()
        );
    }

    @Bean
    public CourtAllocationConfiguration setUpCourtAllocationConfiguration() {

        Map<String, BigDecimal> courtDistribution = courtDistributionConfig.getDistribution();
        Map<String, Map<String, BigDecimal>> courtsAllocationPerFact = courtDistributionConfig.getFactAllocation();
        Map<String, BigDecimal> divorceCasesRatio = courtDistributionConfig.getDivorceCasesRatio();

        return new CourtAllocationConfiguration(
            courtDistribution,
            divorceCasesRatio,
            courtsAllocationPerFact
        );
    }
}