package uk.gov.hmcts.reform.divorce.orchestration.util.nfd;

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
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;
import uk.gov.hmcts.reform.idam.client.models.TokenExchangeResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Base64;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Service
@RequiredArgsConstructor
@Slf4j
public class NfdIdamService {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTH_TYPE = "code";
    public static final String GRANT_TYPE = "authorization_code";
    public static final String BASIC_AUTH_TYPE = "Basic";
    public static final String BEARER_AUTH_TYPE = "Bearer";
    public static final String CODE = "code";
    private final RestTemplate restTemplate;

    @Value("${idam.api.userdetails}")
    private String userDetailsUrl;

    @Value("${idam.api.authorize}")
    private String authorizeUrl;

    @Value("${idam.api.token}")
    private String tokenUrl;

    @Value("${idam.xui.client.id}")
    private String xuiClientId;

    @Value("${idam.xui.client.secret}")
    private String xuiClientSecret;

    @Value("${idam.xui.client.redirect_uri}")
    private String xuiRedirectUri;

    @Value("${idam.caseworker.username}")
    private String caseworkerUserName;

    @Value("${idam.caseworker.password}")
    private String caseworkerPassword;

    public UserDetails getUserDetail(String userId, String authToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, authToken);
        HttpEntity<RestRequest> httpEntity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder.fromHttpUrl(userDetailsUrl)
            .path(userId)
            .encode()
            .toUriString();

        ResponseEntity<UserDetails> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, UserDetails.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to get user details from idam for userId {}", userId);
            throw new RuntimeException(String.format("Unexpected code from Idam: %s ", response.getStatusCode()));
        }
        return response.getBody();
    }

    public String getXuiCaseworkerToken() {
        String code = this.generateCodeForUser(caseworkerUserName, caseworkerPassword);
        return exchangeToken(code);
    }


    private String generateCodeForUser(String username, String password) {
        String authorisation = username + ":" + password;
        String base64Authorisation = Base64.getEncoder().encodeToString(authorisation.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, base64Authorisation);
        HttpEntity<RestRequest> httpEntity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder.fromHttpUrl(authorizeUrl)
            .queryParam("response_type", AUTH_TYPE)
            .queryParam("client_id", xuiClientId)
            .queryParam("client_secret", xuiClientSecret)
            .queryParam("redirect_uri", xuiRedirectUri)
            .queryParam("scope", "openid profile roles manage-user")
            .encode()
            .toUriString();
        log.info("Url built for code generation {} ", url);

        ResponseEntity<AuthenticateUserResponse> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, AuthenticateUserResponse.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to get code for username {} and pwd {}", username, password);
            throw new RuntimeException(String.format("Unexpected code from Idam: %s ", response.getStatusCode()));
        }
        return response.getBody().getCode();
    }

    private String exchangeToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, "application/x-www-form-urlencoded");
        HttpEntity<RestRequest> httpEntity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder.fromHttpUrl(tokenUrl)
            .queryParam("code", code)
            .queryParam("client_id", xuiClientId)
            .queryParam("redirect_uri", xuiRedirectUri)
            .queryParam("grant_type", GRANT_TYPE)
            .encode()
            .toUriString();
        log.info("Url built for token generation {} ", url);

        ResponseEntity<TokenExchangeResponse> tokenExchangeResponse =
            restTemplate.exchange(url, HttpMethod.POST, httpEntity, TokenExchangeResponse.class);
        if (tokenExchangeResponse.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to get token for code {}", code);
            throw new RuntimeException(String.format("Unexpected code from Idam: %s ", tokenExchangeResponse.getStatusCode()));
        }

        return BEARER_AUTH_TYPE + " " + tokenExchangeResponse.getBody().getAccessToken();
    }
}