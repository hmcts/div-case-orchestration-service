package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;

import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AssignCaseAccessRequest;

import java.net.URI;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_URL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.RestService.buildUri;

@RunWith(SpringRunner.class)
public class RestServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private RestService restService;

    @Captor
    private ArgumentCaptor<URI> uriCaptor;
    @Captor private ArgumentCaptor<HttpEntity> authRequestCaptor;

    private AssignCaseAccessRequest body;

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);

        body = AssignCaseAccessRequest
            .builder()
            .caseId(TEST_CASE_ID)
            .caseTypeId(TEST_CASE_ID)
            .assigneeId(TEST_USER_ID)
            .build();
    }

    @Test
    public void restApiPostCall() {
        mockRestTemplateForMethod(HttpMethod.POST);

        restService.restApiPostCall(AUTH_TOKEN, TEST_URL, body);

        assertRestApiCall(HttpMethod.POST);

        assertThat(authRequestCaptor.getValue().getBody(), is(body));
    }

    @Test
    public void restApiDeleteCall() {
        mockRestTemplateForMethod(HttpMethod.DELETE);

        restService.restApiDeleteCall(AUTH_TOKEN, TEST_URL, body);

        assertRestApiCall(HttpMethod.DELETE);

        assertThat(authRequestCaptor.getValue().getBody(), is(body));
    }

    private void mockRestTemplateForMethod(HttpMethod delete) {
        when(restTemplate
            .exchange(eq(buildUri(TEST_URL)), eq(delete), any(HttpEntity.class), eq(Map.class))
        ).thenReturn(ResponseEntity.ok().build());
    }

    private void assertRestApiCall(HttpMethod httpMethod) {
        verify(restTemplate)
            .exchange(uriCaptor.capture(), eq(httpMethod), authRequestCaptor.capture(), eq(Map.class));
        verify(authTokenGenerator).generate();

        HttpHeaders headers = authRequestCaptor.getValue().getHeaders();

        assertThat(uriCaptor.getValue(), is(URI.create(TEST_URL)));
        assertThat(headers.get(AUTHORIZATION_HEADER).get(0), is(AUTH_TOKEN));
        assertThat(headers.get(SERVICE_AUTHORIZATION_HEADER).get(0), is(TEST_SERVICE_TOKEN));
        assertThat(headers.get(HttpHeaders.CONTENT_TYPE).get(0), is(MediaType.APPLICATION_JSON_VALUE));
    }
}
