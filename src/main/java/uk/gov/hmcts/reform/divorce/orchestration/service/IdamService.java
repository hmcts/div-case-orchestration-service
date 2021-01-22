package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.divorce.orchestration.config.IdamServiceConfiguration;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_HEADER;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdamService {

    private final IdamServiceConfiguration serviceConfig;
    private final RestTemplate restTemplate;

    private static final Function<IdamServiceConfiguration, URI> uriSupplier =
        serviceConfig -> fromHttpUrl(serviceConfig.getUrl() + serviceConfig.getApi()).build().toUri();

    private static final Function<ResponseEntity<Map>, String> userId = responseEntity -> {
        Map body = responseEntity.getBody();
        return (String) body.get("id");
    };

    private static final Function<String, HttpEntity> buildAuthRequest = authToken -> {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, authToken);
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(headers);
    };

    public String getIdamUserId(String authorisationToken) {
        return userId.apply(restTemplate.exchange(uriSupplier.apply(serviceConfig), HttpMethod.GET,
            buildAuthRequest.apply(authorisationToken), Map.class));
    }
}
