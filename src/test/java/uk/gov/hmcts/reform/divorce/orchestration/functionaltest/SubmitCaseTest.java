package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonPath;
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
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.CourtLookupService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
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

    private static final String API_URL = "/submit";

    private static final String CCD_FORMAT_CONTEXT_PATH = "/caseformatter/version/1/to-ccd-format";
    private static final String VALIDATION_CONTEXT_PATH = "/version/1/validate";
    private static final String SUBMISSION_CONTEXT_PATH = "/casemaintenance/version/1/submit";
    private static final String DELETE_DRAFT_CONTEXT_PATH = "/casemaintenance/version/1/drafts";
    private static final String RETRIEVE_CASE_CONTEXT_PATH = "/casemaintenance/version/1/case";

    private static final String COURT_ID_JSON_PATH = "$.courts";

    private static final String AUTH_TOKEN = "authToken";

    private static final String FORM_ID = "case-progression";

    private static final Map<String, Object> CASE_DATA = Collections.emptyMap();

    private static final ValidationRequest validationRequest = ValidationRequest.builder()
        .data(CASE_DATA)
        .formId(FORM_ID)
        .build();

    private static final ValidationResponse validationResponse = ValidationResponse.builder().build();

    @Autowired
    private CourtLookupService courtLookupService;

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
        stubMaintenanceServerEndpointForRetrieve(HttpStatus.NOT_FOUND, null);
        stubFormatterServerEndpoint();
        stubValidationServerEndpoint();
        stubMaintenanceServerEndpointForSubmit(Collections.singletonMap(ID, TEST_CASE_ID));
        stubMaintenanceServerEndpointForDeleteDraft(HttpStatus.OK);

        MvcResult result = webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(CASE_DATA))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
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

        String allocatedCourtId = JsonPath.read(responseBody, "$.allocatedCourt.courtId");
        Court allocatedCourt = courtLookupService.getCourtByKey(allocatedCourtId);
        assertThat(responseBody, hasJsonPath("$.allocatedCourt", allOf(
            hasJsonPath("courtId", is(allocatedCourtId)),
            hasJsonPath("serviceCentreName", is(allocatedCourt.getServiceCentreName())),
            hasJsonPath("divorceCentre", is(allocatedCourt.getDivorceCentreName())),
            hasJsonPath("divorceCentreAddressName", is(allocatedCourt.getDivorceCentreAddressName())),
            hasJsonPath("street", is(allocatedCourt.getStreet())),
            hasJsonPath("courtCity", is(allocatedCourt.getCourtCity())),
            hasJsonPath("poBox", is(allocatedCourt.getPoBox())),
            hasJsonPath("postCode", is(allocatedCourt.getPostCode())),
            hasJsonPath("openingHours", is(allocatedCourt.getOpeningHours())),
            hasJsonPath("email", is(allocatedCourt.getEmail())),
            hasJsonPath("phoneNumber", is(allocatedCourt.getPhoneNumber())),
            hasJsonPath("siteId", is(allocatedCourt.getSiteId())),
            hasNoJsonPath("formattedAddress")
        )));
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
            .content(convertObjectToJsonString(CASE_DATA))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
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
            .content(convertObjectToJsonString(CASE_DATA))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenInvalidCaseDataAndAuth_whenCaseDataIsSubmitted_thenReturnBadRequest() throws Exception {
        List<String> validationErrors = Collections.singletonList("An error has occurred");
        validationResponse.setErrors(validationErrors);

        stubFormatterServerEndpoint();
        stubValidationServerEndpoint();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(CASE_DATA))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError());
    }

    private void stubFormatterServerEndpoint() {
        formatterServiceServer.stubFor(WireMock.post(CCD_FORMAT_CONTEXT_PATH)
            .withRequestBody(matchingJsonPath(COURT_ID_JSON_PATH))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(CASE_DATA))));
    }

    private void stubValidationServerEndpoint() {
        validationServiceServer.stubFor(WireMock.post(VALIDATION_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(validationRequest)))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(validationResponse))));
    }

    private void stubMaintenanceServerEndpointForSubmit(Map<String, Object> response) {
        maintenanceServiceServer.stubFor(WireMock.post(SUBMISSION_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(CASE_DATA)))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubMaintenanceServerEndpointForRetrieve(HttpStatus status, Map<String, Object> response) {
        maintenanceServiceServer.stubFor(get(urlEqualTo(RETRIEVE_CASE_CONTEXT_PATH))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubMaintenanceServerEndpointForDeleteDraft(HttpStatus status) {
        maintenanceServiceServer.stubFor(WireMock.delete(DELETE_DRAFT_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())));
    }

}