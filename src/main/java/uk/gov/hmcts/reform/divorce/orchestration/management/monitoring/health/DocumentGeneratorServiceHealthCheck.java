package uk.gov.hmcts.reform.divorce.orchestration.management.monitoring.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DocumentGeneratorServiceHealthCheck extends WebServiceHealthCheck {
    @Autowired
    public DocumentGeneratorServiceHealthCheck(HttpEntityFactory httpEntityFactory, RestTemplate restTemplate,
                                               @Value("${document.generator.service.api.baseurl}/health") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}
