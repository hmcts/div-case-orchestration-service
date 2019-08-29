package uk.gov.hmcts.reform.divorce.orchestration.client;

import feign.Client;
import feign.Response;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.AuthenticateUserResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.PinRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.TokenExchangeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
    name = "idamClient",
    url = "${idam.api.url}",
    configuration = IdamClient.Configuration.class
)
public interface IdamClient {

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/pin",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Pin createPin(@RequestBody PinRequest request,
                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);

    @RequestMapping(method = RequestMethod.GET, value = "/details")
    UserDetails retrieveUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/oauth2/authorize",
        headers = CONTENT_TYPE + "=" + APPLICATION_FORM_URLENCODED_VALUE,
        consumes = APPLICATION_FORM_URLENCODED_VALUE
    )
    AuthenticateUserResponse authenticateUser(
        @RequestHeader(HttpHeaders.AUTHORIZATION) final String authorisation,
        @RequestParam("response_type") final String responseType,
        @RequestParam("client_id") final String clientId,
        @RequestParam("redirect_uri") final String redirectUri
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/oauth2/token",
        headers = CONTENT_TYPE + "=" + APPLICATION_FORM_URLENCODED_VALUE,
        consumes = APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenExchangeResponse exchangeCode(
        @RequestParam("code") final String code,
        @RequestParam("grant_type") final String grantType,
        @RequestParam("redirect_uri") final String redirectUri,
        @RequestParam("client_id") final String clientId,
        @RequestParam("client_secret") final String clientSecret
    );

    @RequestMapping(
        method = RequestMethod.GET,
        value = "/pin",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    Response authenticatePinUser(
        @RequestHeader("pin") final String pin,
        @RequestParam("client_id") final String clientId,
        @RequestParam("redirect_uri") final String redirectUri
    );

    class Configuration {
        @Bean
        public Client getFeignHttpClient() {
            return new ApacheHttpClient(getHttpClient());
        }

        private CloseableHttpClient getHttpClient() {
            int timeout = 10000;
            RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();

            return HttpClientBuilder
                .create()
                .useSystemProperties()
                .disableRedirectHandling()
                .setDefaultRequestConfig(config)
                .build();
        }
    }
}
