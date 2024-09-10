package uk.gov.hmcts.reform.divorce.orchestration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class HttpConnectionConfiguration {

    @Value("${http.connect.timeout}")
    private int httpConnectTimeout;

    @Value("${http.connect.request.timeout}")
    private int httpConnectRequestTimeout;

    @Value("${health.check.http.connect.timeout}")
    private int healthHttpConnectTimeout;

    @Value("${health.check.http.connect.request.timeout}")
    private int healthHttpConnectRequestTimeout;

    @Bean
    @Primary
    public MappingJackson2HttpMessageConverter jackson2HttpConverter(@Autowired ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter jackson2HttpConverter
            = new MappingJackson2HttpMessageConverter(objectMapper);
        jackson2HttpConverter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));
        return jackson2HttpConverter;
    }

    @Bean
    public RestTemplate restTemplate() {
        return getRestTemplate(httpConnectTimeout, httpConnectRequestTimeout);
    }

    @Bean
    public RestTemplate healthCheckRestTemplate() {
        return getRestTemplate(healthHttpConnectTimeout, healthHttpConnectRequestTimeout);
    }

    private RestTemplate getRestTemplate(int connectTimeout, int connectRequestTimeout) {
        SimpleClientHttpRequestFactory clientHttpRequestFactory  = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(connectTimeout);
        clientHttpRequestFactory.setReadTimeout(connectRequestTimeout);

        return new RestTemplate(clientHttpRequestFactory);
    }
}
