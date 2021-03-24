package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.model.response.ValidationResponse;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.CourtLookupService;
import uk.gov.hmcts.reform.divorce.validation.service.ValidationService;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getExpectedTranslatedCoreCaseData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getExpectedTranslatedCoreCaseDataRepresentedRespondentJourneyEnabled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.DataTransformationTestHelper.getTestDivorceSessionData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class SubmitCaseTest extends MockedFunctionalTest {

    private static final String API_URL = "/submit";

    private static final String SUBMISSION_CONTEXT_PATH = "/casemaintenance/version/1/submit";
    private static final String DELETE_DRAFT_CONTEXT_PATH = "/casemaintenance/version/1/drafts";
    private static final String RETRIEVE_CASE_CONTEXT_PATH = "/casemaintenance/version/1/case";

    private DivorceSession testDivorceSessionData;
    private CoreCaseData expectedTranslatedCcdSessionData;
    private CoreCaseData expectedTranslatedCcdSessionDataRepresentedRespondentJourneyEnabled;

    private static final ValidationResponse validationResponseOk = ValidationResponse.builder().build();
    private static final ValidationResponse validationResponseFail = ValidationResponse.builder()
        .errors(Collections.singletonList("An error has occurred"))
        .warnings(Collections.singletonList("Warning!"))
        .build();

    @Autowired
    private CourtLookupService courtLookupService;

    @Autowired
    private MockMvc webClient;

    @MockBean
    private ValidationService validationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Before
    public void setUp() throws IOException {
        testDivorceSessionData = getTestDivorceSessionData();
        expectedTranslatedCcdSessionData = getExpectedTranslatedCoreCaseData();
        expectedTranslatedCcdSessionDataRepresentedRespondentJourneyEnabled =
            getExpectedTranslatedCoreCaseDataRepresentedRespondentJourneyEnabled();
    }

    @Test
    public void givenCaseDataAndAuth_whenCaseDataIsSubmitted_thenReturnSuccess() throws Exception {
        stubMaintenanceServerEndpointForRetrieve(HttpStatus.NOT_FOUND, null);
        stubMaintenanceServerEndpointForSubmit(Collections.singletonMap(ID, TEST_CASE_ID));
        stubMaintenanceServerEndpointForDeleteDraft(HttpStatus.OK);
        when(validationService.validate(any())).thenReturn(validationResponseOk);

        MvcResult result = webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(testDivorceSessionData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody, allOf(
            isJson(),
            hasJsonPath("$.caseId", equalTo(TEST_CASE_ID)),
            hasJsonPath("$.status", equalTo(SUCCESS_STATUS)),
            hasJsonPath("$.allocatedCourt",
                hasJsonPath("courtId", is(notNullValue()))
            )
        ));

        Map actualAllocatedCourt = JsonPath.read(responseBody, "$.allocatedCourt");
        String allocatedCourtId = JsonPath.read(actualAllocatedCourt, "$.courtId");
        Court expectedAllocatedCourt = courtLookupService.getCourtByKey(allocatedCourtId);

        JSONAssert.assertEquals(convertObjectToJsonString(expectedAllocatedCourt), convertObjectToJsonString(actualAllocatedCourt), true);
    }

    @Test
    public void givenCaseDataAndAuth_whenRespSolDigital_thenUpdateRespDigitalDetails() throws Exception {
        stubMaintenanceServerEndpointForRetrieve(HttpStatus.NOT_FOUND, null);
        stubMaintenanceServerEndpointForSubmitRepResp(Collections.singletonMap(ID, TEST_CASE_ID));
        stubMaintenanceServerEndpointForDeleteDraft(HttpStatus.OK);
        when(validationService.validate(any())).thenReturn(validationResponseOk);
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(testDivorceSessionData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @Test
    public void givenCaseAlreadyExists_whenCaseDataIsSubmitted_thenReturnSuccess() throws Exception {
        Map<String, Object> retrieveCaseResponse = ImmutableMap.<String, Object>builder()
            .put(ID, TEST_CASE_ID)
            .put(CASE_STATE_JSON_KEY, AWAITING_PAYMENT)
            .put("case_data", Collections.singletonMap(DIVORCE_UNIT_JSON_KEY, "serviceCentre"))
            .build();

        stubMaintenanceServerEndpointForRetrieve(HttpStatus.OK, retrieveCaseResponse);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(testDivorceSessionData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.caseId", equalTo(TEST_CASE_ID)),
                hasJsonPath("$.status", equalTo(SUCCESS_STATUS)),
                hasJsonPath("$.allocatedCourt", allOf(
                    hasJsonPath("courtId", equalTo("serviceCentre")),
                    hasJsonPath("serviceCentreName", equalTo("Courts and Tribunals Service Centre"))
                ))
            )));
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
            .content(convertObjectToJsonString(testDivorceSessionData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidCaseDataAndAuth_whenCaseDataIsSubmitted_thenReturnBadRequest() throws Exception {
        when(validationService.validate(any())).thenReturn(validationResponseFail);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(testDivorceSessionData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
    }

    private void stubMaintenanceServerEndpointForSubmit(Map<String, Object> response) {
        String testCcdTranslatedData = convertObjectToJsonString(expectedTranslatedCcdSessionData);
        maintenanceServiceServer.stubFor(WireMock.post(SUBMISSION_CONTEXT_PATH)
            .withRequestBody(equalToJson(testCcdTranslatedData))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubMaintenanceServerEndpointForSubmitRepResp(Map<String, Object> response) {
        String testCcdTranslatedData = convertObjectToJsonString(expectedTranslatedCcdSessionDataRepresentedRespondentJourneyEnabled);
        maintenanceServiceServer.stubFor(WireMock.post(SUBMISSION_CONTEXT_PATH)
            .withRequestBody(equalToJson(testCcdTranslatedData))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubMaintenanceServerEndpointForRetrieve(HttpStatus status, Map<String, Object> response) {
        maintenanceServiceServer.stubFor(get(urlEqualTo(RETRIEVE_CASE_CONTEXT_PATH))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubMaintenanceServerEndpointForDeleteDraft(HttpStatus status) {
        maintenanceServiceServer.stubFor(WireMock.delete(DELETE_DRAFT_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())));
    }
}
