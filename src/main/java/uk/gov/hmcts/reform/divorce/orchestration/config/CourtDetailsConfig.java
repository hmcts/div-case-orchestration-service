package uk.gov.hmcts.reform.divorce.orchestration.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties(prefix = "court")
@Validated
@Getter
public class CourtDetailsConfig {
    @NotNull
    private Map<String, Court> locations = new HashMap<>();
}
