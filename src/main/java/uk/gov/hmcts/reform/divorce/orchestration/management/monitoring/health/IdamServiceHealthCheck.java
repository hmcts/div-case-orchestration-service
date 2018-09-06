package uk.gov.hmcts.reform.divorce.orchestration.management.monitoring.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IdamServiceHealthCheck extends WebServiceHealthCheck {
    @Autowired
    public IdamServiceHealthCheck(HttpEntityFactory httpEntityFactory, RestTemplate restTemplate,
                                  @Value("${idam.api.url}/health") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}