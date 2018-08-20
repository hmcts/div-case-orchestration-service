package uk.gov.hmcts.reform.divorce.orchestration.management.monitoring.health;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class DefaultHttpEntityFactory implements HttpEntityFactory {
    @Override
    public HttpEntity<Object> createRequestEntityForHealthCheck() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));

        return new HttpEntity<>(headers);
    }
}
