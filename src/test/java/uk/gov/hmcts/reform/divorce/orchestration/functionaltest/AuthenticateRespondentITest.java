package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthenticateRespondentITest {
    private static final String API_URL = "/authenticate-respondent";
    private static final String IDAM_USER_DETAILS_CONTEXT_PATH = "/details";

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule idamUserDetailsServer = new WireMockClassRule(4503);

    @Test
    public void givenAuthTokenIsNull_whenAuthenticateRespondent_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidUserToken_whenAuthenticateRespondent_thenReturnUnauthorized() throws Exception {
        final String errorMessage = "error message";

        stubUserDetailsEndpoint(HttpStatus.UNAUTHORIZED, BEARER_AUTH_TOKEN, errorMessage);

        webClient.perform(post(API_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(containsString(errorMessage)));
    }

    @Test
    public void givenUserDoesNotHaveAnyRole_whenAuthenticateRespondent_thenReturnUnauthorized() throws Exception {
        final String userDetailsResponse = getUserDetailsResponse(Collections.emptyList());

        stubUserDetailsEndpoint(HttpStatus.OK, BEARER_AUTH_TOKEN, userDetailsResponse);

        webClient.perform(post(API_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenUserDoesNotHaveLetterHolderRole_whenAuthenticateRespondent_thenReturnUnauthorized()
        throws Exception {
        final String userDetailsResponse = getUserDetailsResponse(Arrays.asList("letter-holder", "letter-loa1"));

        stubUserDetailsEndpoint(HttpStatus.OK, BEARER_AUTH_TOKEN, userDetailsResponse);

        webClient.perform(post(API_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenUserHasLetterHolderRole_whenAuthenticateRespondent_thenReturnOk() throws Exception {
        final String userDetailsResponse = getUserDetailsResponse(
            Arrays.asList("letter-holder", "letter-loa1", "letter-12345"));

        stubUserDetailsEndpoint(HttpStatus.OK, BEARER_AUTH_TOKEN, userDetailsResponse);

        webClient.perform(post(API_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk());
    }

    private void stubUserDetailsEndpoint(HttpStatus status, String authHeader, String message) {
        idamUserDetailsServer.stubFor(get(IDAM_USER_DETAILS_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(authHeader))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(message)));
    }

    private String getUserDetailsResponse(List<String> roles) {
        return ObjectMapperTestUtil.convertObjectToJsonString(
            UserDetails.builder()
                .roles(roles)
                .build());
    }
}
