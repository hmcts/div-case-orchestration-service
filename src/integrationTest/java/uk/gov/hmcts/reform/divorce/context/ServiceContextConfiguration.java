package uk.gov.hmcts.reform.divorce.context;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.google.common.collect.ImmutableList;
import feign.Feign;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Decoder;
import feign.jackson.JacksonEncoder;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.support.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.support.IdamUtils;
import uk.gov.hmcts.reform.divorce.support.cms.CmsClientSupport;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.support.cos.DraftsSubmissionSupport;

@Lazy
@Configuration
@PropertySource("classpath:application.properties")
@PropertySource("classpath:application-${env}.properties")
public class ServiceContextConfiguration {

    @Bean
    public IdamUtils getIdamUtil() {
        return new IdamUtils();
    }

    @Bean
    public CcdClientSupport getCcdClientSupport() {
        return new CcdClientSupport();
    }

    @Bean
    public DraftsSubmissionSupport getDraftSubmissionSupport() {
        return new DraftsSubmissionSupport();
    }

    @Bean
    public CmsClientSupport getCmsClientSupport() {
        return new CmsClientSupport();
    }

    @Bean("ccdSubmissionTokenGenerator")
    public AuthTokenGenerator ccdSubmissionAuthTokenGenerator(
            @Value("${auth.provider.ccdsubmission.client.key}") final String secret,
            @Value("${auth.provider.ccdsubmission.microservice}") final String microService,
            @Value("${idam.s2s-auth.url}") final String s2sUrl
    ) {
        final ServiceAuthorisationApi serviceAuthorisationApi = Feign.builder()
                .encoder(new JacksonEncoder())
                .contract(new SpringMvcContract())
                .target(ServiceAuthorisationApi.class, s2sUrl);

        return new ServiceAuthTokenGenerator(secret, microService, serviceAuthorisationApi);
    }

    @Bean("documentGeneratorTokenGenerator")
    public AuthTokenGenerator ccdDocumentGeneratorAuthTokenGenerator(
            @Value("${auth.provider.documentgenerator.client.key}") final String secret,
            @Value("${auth.provider.documentgenerator.microservice}") final String microService,
            @Value("${idam.s2s-auth.url}") final String s2sUrl
    ) {
        final ServiceAuthorisationApi serviceAuthorisationApi = Feign.builder()
                .encoder(new JacksonEncoder())
                .contract(new SpringMvcContract())
                .target(ServiceAuthorisationApi.class, s2sUrl);

        return new ServiceAuthTokenGenerator(secret, microService, serviceAuthorisationApi);
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            if(template.request().httpMethod() == Request.HttpMethod.POST) {
                template.header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            }
        };
    }

    @Bean
    public CoreCaseDataApi getCoreCaseDataApi(
            @Value("${core_case_data.api.url}") final String coreCaseDataApiUrl) {
        return Feign.builder()
                .requestInterceptor(requestInterceptor())
                .encoder(new JacksonEncoder())
                .decoder(feignDecoder())
                .contract(new SpringMvcContract())
                .target(CoreCaseDataApi.class, coreCaseDataApiUrl);
    }

    @Bean
    public CaseMaintenanceClient getCaseMaintenanceClient(
            @Value("${case_maintenance.api.url}") final String caseMaintenanceUrl) {
        return Feign.builder()
                .requestInterceptor(requestInterceptor())
                .encoder(new JacksonEncoder())
                .decoder(feignDecoder())
                .contract(new SpringMvcContract())
                .target(CaseMaintenanceClient.class, caseMaintenanceUrl);
    }

    @Bean
    public CosApiClient getCosApiClient(
            @Value("${case.orchestration.service.base.uri}") final String cosApiClientUrl) {
        return Feign.builder()
                .requestInterceptor(requestInterceptor())
                .encoder(new JacksonEncoder())
                .decoder(feignDecoder())
                .contract(new SpringMvcContract())
                .target(CosApiClient.class, cosApiClientUrl);
    }

    @Bean
    public Decoder feignDecoder() {
        MappingJackson2HttpMessageConverter jacksonConverter =
                new MappingJackson2HttpMessageConverter(customObjectMapper());
        jacksonConverter.setSupportedMediaTypes(ImmutableList.of(MediaType.APPLICATION_JSON));

        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(jacksonConverter);
        return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
    }

    @Bean
    public ObjectMapper customObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR310Module());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        return objectMapper;
    }
}


