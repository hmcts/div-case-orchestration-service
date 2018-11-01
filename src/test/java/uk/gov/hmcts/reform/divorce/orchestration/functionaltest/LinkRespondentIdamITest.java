package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN_1;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.START_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class LinkRespondentIdamITest extends IdamTestSupport {
    private static final String API_URL = "/link-respondent/" + TEST_CASE_ID + "/" + TEST_PIN;
    private static final String LINK_RESPONDENT_CONTEXT_PATH = "/casemaintenance/version/1/link-respondent/"
        + TEST_CASE_ID + "/" + TEST_LETTER_HOLDER_ID_CODE;
    private static final String UPDATE_CONTEXT_PATH = String.format(
        "/casemaintenance/version/1/updateCase/%s/%s",
        TEST_CASE_ID,
        START_AOS_EVENT_ID
    );

    @Value("${aos.responded.days-to-complete}")
    private int daysToComplete;

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule maintenanceServiceServer = new WireMockClassRule(4010);

    private Map<String, Object> caseData;

    @Before
    public void setup() {
        caseData = ImmutableMap.of(
            RESPONDENT_EMAIL_ADDRESS, TEST_EMAIL,
            RECEIVED_AOS_FROM_RESP, YES_VALUE,
            RECEIVED_AOS_FROM_RESP_DATE, CcdUtil.getCurrentDate(),
            CCD_DUE_DATE, CcdUtil.getCurrentDatePlusDays(daysToComplete)
        );
    }

    @Test
    public void givenAuthTokenIsNull_whenLinkRespondent_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenPinAuthFails_whenLinkRespondent_thenReturnBadRequest() throws Exception {
        stubPinAuthoriseEndpoint(UNAUTHORIZED, TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenExchangeCodeFails_whenLinkRespondent_thenReturnBadRequest() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(BAD_REQUEST, TEST_CODE, TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenUserDetailsThrowException_whenLinkRespondent_thenReturnBadRequest() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(BAD_REQUEST, BEARER_AUTH_TOKEN_1, TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenUserDetailsReturnNull_whenLinkRespondent_thenReturnUnAuthorized() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, null);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenLinkFailsInCms_whenLinkRespondent_thenPropagateException() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, USER_DETAILS_PIN_USER_JSON);
        stubMaintenanceServerEndpointForLinkRespondent(UNAUTHORIZED);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenGetRespondentDetailsFails_whenLinkRespondent_thenPropagateException() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, USER_DETAILS_PIN_USER_JSON);
        stubMaintenanceServerEndpointForLinkRespondent(OK);
        stubUserDetailsEndpoint(UNAUTHORIZED, BEARER_AUTH_TOKEN, TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenUpdateRespondentDetailsFails_whenLinkRespondent_thenPropagateException() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, USER_DETAILS_PIN_USER_JSON);
        stubMaintenanceServerEndpointForLinkRespondent(OK);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN, USER_DETAILS_JSON);
        stubMaintenanceServerEndpointForUpdate(BAD_REQUEST, TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenAllGoesWell_whenLinkRespondent_thenProceedAsExpected() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, USER_DETAILS_PIN_USER_JSON);
        stubMaintenanceServerEndpointForLinkRespondent(OK);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN, USER_DETAILS_JSON);
        stubMaintenanceServerEndpointForUpdate(OK, convertObjectToJsonString(caseData));

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }


    private void stubMaintenanceServerEndpointForLinkRespondent(HttpStatus status) {
        maintenanceServiceServer.stubFor(post(LINK_RESPONDENT_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, new EqualToPattern(APPLICATION_JSON_VALUE))
            .willReturn(aResponse()
                .withStatus(status.value())
            ));
    }

    private void stubMaintenanceServerEndpointForUpdate(HttpStatus status, String response) {
        maintenanceServiceServer.stubFor(post(UPDATE_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(caseData)))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(response)));
    }

}
