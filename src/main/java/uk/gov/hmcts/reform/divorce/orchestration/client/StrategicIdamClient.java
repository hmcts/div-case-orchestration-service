package uk.gov.hmcts.reform.divorce.orchestration.client;

import feign.Client;
import feign.Response;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "strategicIdamClient",
    url = "${idam.api.url}",
    qualifier = "strategicIdamClient",
    configuration = StrategicIdamClient.Configuration.class)
public interface StrategicIdamClient extends IdamClient {
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
