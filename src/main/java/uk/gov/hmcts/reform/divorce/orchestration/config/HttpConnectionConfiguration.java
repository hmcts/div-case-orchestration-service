package uk.gov.hmcts.reform.divorce.orchestration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.logging.httpcomponents.OutboundRequestIdSettingInterceptor;

import static java.util.Arrays.asList;

@Configuration
public class HttpConnectionConfiguration {

    @Value("${http.connect.timeout}")
    private int httpConnectTimeout;

    @Value("${http.connect.request.timeout}")
    private int httpConnectRequestTimeout;

    @Value("${http.connect.max-connections:10}")
    private int httpMaxConnections;

    @Value("${health.check.http.connect.timeout}")
    private int healthHttpConnectTimeout;

    @Value("${health.check.http.connect.request.timeout}")
    private int healthHttpConnectRequestTimeout;

    @Value("${health.check.http.connect.max-connections:10}")
    private int healthHttpMaxConnections;

    @Bean
    @Primary
    public MappingJackson2HttpMessageConverter jackson2HttpCoverter(@Autowired ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter jackson2HttpConverter
            = new MappingJackson2HttpMessageConverter(objectMapper);
        jackson2HttpConverter.setSupportedMediaTypes(ImmutableList.of(MediaType.APPLICATION_JSON));
        return jackson2HttpConverter;
    }

    @Bean
    public RestTemplate restTemplate(@Autowired MappingJackson2HttpMessageConverter jackson2HttpConverter) {

        RestTemplate restTemplate = new RestTemplate(asList(jackson2HttpConverter,
            new FormHttpMessageConverter(),
            new ResourceHttpMessageConverter(),
            new ByteArrayHttpMessageConverter(),
            new StringHttpMessageConverter()));

        restTemplate.setRequestFactory(getClientHttpRequestFactory(httpConnectTimeout, httpConnectRequestTimeout, httpMaxConnections));

        return restTemplate;
    }

    @Bean
    public RestTemplate healthCheckRestTemplate(@Autowired MappingJackson2HttpMessageConverter jackson2HttpConverter) {
        RestTemplate restTemplate = new RestTemplate(asList(jackson2HttpConverter,
            new FormHttpMessageConverter(),
            new ResourceHttpMessageConverter(),
            new ByteArrayHttpMessageConverter(),
            new StringHttpMessageConverter()));

        restTemplate.setRequestFactory(getClientHttpRequestFactory(healthHttpConnectTimeout, healthHttpConnectRequestTimeout,
            healthHttpMaxConnections));

        return restTemplate;
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory(int httpConnectTimeout, int httpConnectRequestTimeout, int maxConnections) {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(httpConnectTimeout)
            .setConnectionRequestTimeout(httpConnectRequestTimeout)
            .setSocketTimeout(httpConnectRequestTimeout)
            .build();

        CloseableHttpClient client = HttpClientBuilder
            .create()
            .useSystemProperties()
            .addInterceptorFirst(new OutboundRequestIdSettingInterceptor())
            .setDefaultRequestConfig(config)
            .setMaxConnPerRoute(maxConnections)
            .setMaxConnTotal(maxConnections)
            .build();

        return new HttpComponentsClientHttpRequestFactory(client);
    }
}
