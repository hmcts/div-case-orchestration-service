package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationConfiguration;

import java.math.BigDecimal;
import java.util.Map;

@Configuration
public class CourtAllocationConfigurator {

    @Autowired
    private CourtDistributionConfig courtDistributionConfig;

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