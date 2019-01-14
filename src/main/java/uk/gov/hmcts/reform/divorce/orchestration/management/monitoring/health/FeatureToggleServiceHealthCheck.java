package uk.gov.hmcts.reform.divorce.orchestration.management.monitoring.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FeatureToggleServiceHealthCheck extends WebServiceHealthCheck {
    @Autowired
    public FeatureToggleServiceHealthCheck(HttpEntityFactory httpEntityFactory,
                                           @Qualifier("healthCheckRestTemplate") RestTemplate restTemplate,
                                           @Value("${feature-toggle.service.api.baseurl}/health") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}
