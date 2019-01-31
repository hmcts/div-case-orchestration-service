package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationConfiguration;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.DefaultCourtAllocator;

import java.io.IOException;

@Configuration
public class CourtAllocationConfigurator {

    @Autowired
    private CourtAllocationConfiguration courtAllocationConfig;

    private final ObjectMapper objectMapper;

    public CourtAllocationConfigurator(@Autowired ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public CourtAllocationConfiguration setUpEnvironmentCourtAllocationConfiguration(
            @Value("${courtAllocationConfigurationJson}") String courtAllocationConfigJson) throws IOException {
        return objectMapper.readValue(courtAllocationConfigJson, CourtAllocationConfiguration.class);
    }

    @Bean
    public CourtAllocator configureCourtAllocationFromEnvironmentVariable() {
        return new DefaultCourtAllocator(courtAllocationConfig);
    }

}