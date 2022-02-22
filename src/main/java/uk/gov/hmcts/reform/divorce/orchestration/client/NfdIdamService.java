package uk.gov.hmcts.reform.divorce.orchestration.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.rest.RestRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Service
@RequiredArgsConstructor
@Slf4j
public class NfdIdamService {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final RestTemplate restTemplate;

    @Value("${idam.api.userdetails}")
    private String userDetailsUrl;

    public UserDetails getUserDetail(String userId, String authToken) throws CaseOrchestrationServiceException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, authToken);
        HttpEntity<RestRequest> httpEntity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder.fromHttpUrl(userDetailsUrl)
            .path(userId)
            .encode()
            .toUriString();

        ResponseEntity<UserDetails> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, UserDetails.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to get user details from idam for userId {} and token {}", userId, authToken);
            throw new CaseOrchestrationServiceException(String.format("Unexpected code from Idam: %s ", response.getStatusCode()));
        }
        return response.getBody();
    }
}
