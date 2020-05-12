package uk.gov.hmcts.reform.divorce.orchestration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.Map;

@Setter
@Getter
@Component
@ConfigurationProperties
public class EventConfig {
    private Map<LanguagePreference, Map<EventType, String>> events;
}
