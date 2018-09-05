package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SubmitCaseTest {

    private static final String API_URL = "/transformationapi/version/1/submit";

    private static final String CCD_FORMAT_CONTEXT_PATH = "/caseformatter/version/1/to-ccd-format";
    private static final String VALIDATION_CONTEXT_PATH = "/version/1/validate";
    private static final String SUBMISSION_CONTEXT_PATH = "/casemaintenance/version/1/submit";

    private static final String AUTH_TOKEN = "authToken";

    private static final Map<String, Object> CASE_DATA = Collections.emptyMap();

    private static final String FORM_ID = "case-progression";
    private static final String VALIDATION_ERROR =
            "uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData_Error";
    private static final ValidationRequest validationRequest = ValidationRequest.builder()
            .data(CASE_DATA)
            .formId(FORM_ID)
            .build();
    private static final ValidationResponse validationResponse = ValidationResponse.builder().build();

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);

    @ClassRule
    public static WireMockClassRule validationServiceServer = new WireMockClassRule(4008);

    @ClassRule
    public static WireMockClassRule maintenanceServiceServer = new WireMockClassRule(4010);

    @Test
    public void givenCaseDataAndAuth_whenCaseDataIsSubmitted_thenReturnSuccess() throws Exception {
        Map<String, Object> responseData = Collections.singletonMap(ID, TEST_CASE_ID);

        stubFormatterServerEndpoint(CASE_DATA, CASE_DATA);
        stubValidationServerEndpoint(HttpStatus.OK, validationRequest, validationResponse);
        stubMaintenanceServerEndpointForSubmit(CASE_DATA, responseData);

        CaseResponse submissionResonse = CaseResponse.builder()
                .caseId(TEST_CASE_ID)
                .status(SUCCESS_STATUS)
                .build();

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(convertObjectToJsonString(CASE_DATA))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString(convertObjectToJsonString(submissionResonse))));
    }

    @Test
    public void givenNoPayload_whenCaseDataIsSubmitted_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoAuthToken_whenCaseDataIsSubmitted_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(CASE_DATA))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidCaseDataAndAuth_whenCaseDataIsSubmitted_thenReturnBadRequest() throws Exception {
        List<String> validationErrors = Collections.singletonList("An error has occurred");
        validationResponse.setErrors(validationErrors);

        Map<String, Object> errors = Collections.singletonMap(VALIDATION_ERROR, validationResponse);

        stubFormatterServerEndpoint(CASE_DATA, CASE_DATA);
        stubValidationServerEndpoint(HttpStatus.OK, validationRequest, validationResponse);

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(convertObjectToJsonString(CASE_DATA))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    private void stubFormatterServerEndpoint(Map<String, Object> transformToCCDFormat, Map<String, Object> response)
            throws Exception {
        formatterServiceServer.stubFor(WireMock.post(CCD_FORMAT_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(transformToCCDFormat)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(response))));
    }

    private void stubValidationServerEndpoint(HttpStatus status,
                                              ValidationRequest request,
                                              ValidationResponse response)
            throws Exception {
        validationServiceServer.stubFor(WireMock.post(VALIDATION_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(request)))
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(response))));
    }

    private void stubMaintenanceServerEndpointForSubmit(Map<String, Object> caseData, Map<String, Object> response)
            throws Exception {
        maintenanceServiceServer.stubFor(WireMock.post(SUBMISSION_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(caseData)))
                .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(response))));
    }
}
