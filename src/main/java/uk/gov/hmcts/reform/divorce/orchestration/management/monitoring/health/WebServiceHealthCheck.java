package uk.gov.hmcts.reform.divorce.orchestration.management.monitoring.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Slf4j
public abstract class WebServiceHealthCheck implements HealthIndicator {
    private final HttpEntityFactory httpEntityFactory;
    private final RestTemplate restTemplate;
    private final String uri;

    WebServiceHealthCheck(HttpEntityFactory httpEntityFactory,
                          @Qualifier("healthCheckRestTemplate") RestTemplate restTemplate, String uri) {
        this.httpEntityFactory = httpEntityFactory;
        this.restTemplate = restTemplate;
        this.uri = uri;
    }

    public Health health() {
        HttpEntity<Object> httpEntity = httpEntityFactory.createRequestEntityForHealthCheck();
        ResponseEntity<Object> responseEntity;

        try {
            responseEntity = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, Object.class, new HashMap<>());
        } catch (HttpServerErrorException | ResourceAccessException serverException) {
            log.error("Exception occurred while doing health check", serverException);
            return Health.down().build();
        } catch (Exception exception) {
            log.info("Unable to access upstream service", exception);
            return Health.unknown().build();
        }

        return responseEntity.getStatusCode().equals(HttpStatus.OK) ? Health.up().build() : Health.unknown().build();
    }
}
