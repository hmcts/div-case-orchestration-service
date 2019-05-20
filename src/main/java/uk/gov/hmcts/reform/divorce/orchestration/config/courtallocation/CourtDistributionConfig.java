package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "court-distribution-config")
@Getter
@Validated
public class CourtDistributionConfig {
    private Map<String, BigDecimal> distribution = new HashMap<>();
    private Map<String, Map<String, BigDecimal>> factAllocation = new HashMap<>();
    private Map<String, BigDecimal> divorceCasesRatio = new HashMap<>();
}
