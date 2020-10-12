package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.CourtsMatcher;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class RetrieveAosCaseITest extends IdamTestSupport {

    private static final String API_URL = "/retrieve-aos-case";
    private static final String RETRIEVE_AOS_CASE_CONTEXT_PATH = "/casemaintenance/version/1/retrieveAosCase";
    private static final String FORMAT_TO_DIVORCE_CONTEXT_PATH = "/caseformatter/version/1/to-divorce-format";
    private static final String IDAM_USER_DETAILS_URL = "/details";

    private static final Map<String, Object> CASE_DATA = ImmutableMap.of(
        D_8_DIVORCE_UNIT, TEST_COURT,
        D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);

    private static final CaseDetails CASE_DETAILS =
        CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(CASE_DATA)
            .build();

    @Autowired
    private MockMvc webClient;

    @Before
    public void setUp() {
        stubUserDetailsEndpoint(OK, AUTH_TOKEN, USER_DETAILS_JSON);
    }

    @Test
    public void givenNoAuthToken_whenRetrieveAosCase_thenReturnBadRequest() throws Exception {
        webClient.perform(get(API_URL)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCMSThrowsException_whenRetrieveAosCase_thenPropagateException() throws Exception {
        stubRetrieveAosCaseFromCMS(INTERNAL_SERVER_ERROR, TEST_ERROR);

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenNoCaseExists_whenRetrieveAosCase_thenReturnEmptyResponse() throws Exception {
        stubRetrieveAosCaseFromCMS(null);

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenCFSThrowsException_whenRetrieveAosCase_thenPropagateException() throws Exception {
        stubIdamUserDetailsEndpoint(HttpStatus.OK, AUTH_TOKEN, getUserDetailsResponse());
        stubRetrieveAosCaseFromCMS(CASE_DETAILS);

        stubFormatterServerEndpoint(INTERNAL_SERVER_ERROR, TEST_ERROR);

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenAllGoesWellProceedAsExpected() throws Exception {
        stubIdamUserDetailsEndpoint(HttpStatus.OK, AUTH_TOKEN, getUserDetailsResponse());
        stubRetrieveAosCaseFromCMS(CASE_DETAILS);

        stubFormatterServerEndpoint();

        CaseDataResponse expected = CaseDataResponse.builder()
            .data(CASE_DATA)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .court(TEST_COURT)
            .build();

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)))
            .andExpect(content().string(hasJsonPath("$.data.court", CourtsMatcher.isExpectedCourtsList())));
    }

    private void stubRetrieveAosCaseFromCMS(CaseDetails caseDetails) {
        stubRetrieveAosCaseFromCMS(OK, convertObjectToJsonString(caseDetails));
    }

    private void stubRetrieveAosCaseFromCMS(HttpStatus status, String message) {
        maintenanceServiceServer.stubFor(WireMock.get(RETRIEVE_AOS_CASE_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(message)));
    }

    private void stubFormatterServerEndpoint() {
        stubFormatterServerEndpoint(OK, convertObjectToJsonString(CASE_DATA));
    }

    private void stubFormatterServerEndpoint(HttpStatus status, String message) {
        formatterServiceServer.stubFor(WireMock.post(FORMAT_TO_DIVORCE_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(CASE_DATA)))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(message)));
    }

    private void stubIdamUserDetailsEndpoint(HttpStatus status, String authHeader, String message) {
        idamServer.stubFor(WireMock.get(IDAM_USER_DETAILS_URL)
            .withHeader(AUTHORIZATION, new EqualToPattern(authHeader))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(message)));
    }

    private String getUserDetailsResponse() {
        return ObjectMapperTestUtil.convertObjectToJsonString(
            UserDetails.builder()
                .email(TEST_PETITIONER_EMAIL)
                .build());
    }

}