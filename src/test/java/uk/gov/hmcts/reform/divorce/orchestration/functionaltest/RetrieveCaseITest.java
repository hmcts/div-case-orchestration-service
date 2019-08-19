package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class RetrieveCaseITest extends MockedFunctionalTest {
    private static final String API_URL = "/retrieve-case";
    private static final String GET_CASE_CONTEXT_PATH = "/casemaintenance/version/1/case";
    private static final String FORMAT_TO_DIVORCE_CONTEXT_PATH = "/caseformatter/version/1/to-divorce-format";

    private static final Map<String, Object> CASE_DATA = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);
    private static final CaseDetails CASE_DETAILS =
        CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(CASE_DATA)
            .build();

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenNoAuthToken_whenRetrieveCase_thenReturnBadRequest() throws Exception {
        webClient.perform(get(API_URL)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCMSThrowsException_whenRetrieveAosCase_thenPropagateException() throws Exception {
        stubGetCaseFromCMS(HttpStatus.INTERNAL_SERVER_ERROR, TEST_ERROR);

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenNoCaseExists_whenGetCase_thenReturnEmptyResponse() throws Exception {
        stubGetCaseFromCMS(null);

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenCFSThrowsException_whenGetCase_thenPropagateException() throws Exception {
        stubGetCaseFromCMS(CASE_DETAILS);

        stubFormatterServerEndpoint(HttpStatus.INTERNAL_SERVER_ERROR, TEST_ERROR);

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenMultipleCases_whenGetCase_thenPropagateException() throws Exception {
        stubGetMultipleCaseFromCMS();

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isMultipleChoices())
            .andExpect(content().string(""));
    }

    @Test
    public void givenAllGoesWellProceedAsExpected_whenGetCase_thenPropagateException() throws Exception {
        stubGetCaseFromCMS(CASE_DETAILS);

        stubFormatterServerEndpoint();

        CaseDataResponse expected = CaseDataResponse.builder()
            .data(CASE_DATA)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .courts(TEST_COURT)
            .build();

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    private void stubGetCaseFromCMS(CaseDetails caseDetails) {
        stubGetCaseFromCMS(HttpStatus.OK, convertObjectToJsonString(caseDetails));
    }

    private void stubGetCaseFromCMS(HttpStatus status, String message) {
        maintenanceServiceServer.stubFor(WireMock.get(GET_CASE_CONTEXT_PATH)
                .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(message)));
    }

    private void stubGetMultipleCaseFromCMS() {
        stubGetCaseFromCMS(HttpStatus.MULTIPLE_CHOICES, "");
    }

    private void stubFormatterServerEndpoint() {
        stubFormatterServerEndpoint(HttpStatus.OK, convertObjectToJsonString(CASE_DATA));
    }

    private void stubFormatterServerEndpoint(HttpStatus status, String message) {
        formatterServiceServer.stubFor(WireMock.post(FORMAT_TO_DIVORCE_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(CASE_DATA)))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(message)));
    }

}
