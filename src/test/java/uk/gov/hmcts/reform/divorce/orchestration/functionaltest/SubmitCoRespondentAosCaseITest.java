package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DEFENDS_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SUBMISSION_AOS_AWAITING_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SubmitCoRespondentAosCaseITest {
    private static final String API_URL = "/submit-co-respondent-aos";
    private static final String FORMAT_TO_AOS_CASE_CONTEXT_PATH = "/caseformatter/version/1/to-aos-submit-format";
    private static final String UPDATE_CONTEXT_PATH = "/casemaintenance/version/1/updateCase/" + TEST_CASE_ID + "/";
    private static final String RETRIEVE_CASE_CONTEXT_PATH = "/casemaintenance/version/1/retrieveAosCase";

    private final LocalDateTime today = LocalDateTime.now();

    @Autowired
    private MockMvc webClient;

    @MockBean
    private Clock clock;

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);

    @ClassRule
    public static WireMockClassRule maintenanceServiceServer = new WireMockClassRule(4010);

    @Before
    public void setup() {
        when(clock.instant()).thenReturn(today.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(UTC);
    }

    @Test
    public void givenNoAuthToken_whenSubmitCoRespondentAos_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .content(convertObjectToJsonString(emptyMap()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoPayload_whenSubmitCoRespondentAos_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCaseFormatterFails_whenSubmitCoRespondentAos_thenPropagateTheException() throws Exception {
        stubFormatterServerEndpoint(BAD_REQUEST, emptyMap(), TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(emptyMap()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenCaseRetrievalFails_whenSubmitCoRespondentAos_thenPropagateTheException() throws Exception {
        stubFormatterServerEndpoint(OK, emptyMap(), convertObjectToJsonString(emptyMap()));
        stubMaintenanceServerEndpointForAosRetrieval(NOT_FOUND, convertObjectToJsonString(TEST_ERROR));

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(emptyMap()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenCaseIsInWrongState_whenSubmitCoRespondentAos_thenPropagateTheException() throws Exception {
        stubFormatterServerEndpoint(OK, emptyMap(), convertObjectToJsonString(emptyMap()));

        final CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).state("foo").build();
        stubMaintenanceServerEndpointForAosRetrieval(OK, convertObjectToJsonString(caseDetails));

        final String expectedError = String.format("uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.ValidationException: "
            + "Cannot create co-respondent submission event for case [%s] in state [%s].", TEST_CASE_ID, "foo");

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(emptyMap()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(is(expectedError)));
    }

    @Test
    public void givenCaseUpdateFails_whenSubmitCoRespondentAos_thenPropagateTheException() throws Exception {
        stubFormatterServerEndpoint(OK, emptyMap(), convertObjectToJsonString(getCoRespondentSubmitData()));

        final CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).state(AOS_AWAITING).build();
        stubMaintenanceServerEndpointForAosRetrieval(OK, convertObjectToJsonString(caseDetails));

        stubMaintenanceServerEndpointForAosUpdate(BAD_REQUEST, getCoRespondentSubmitData(), TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(emptyMap()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void happyPath() throws Exception {
        final String caseDataString = convertObjectToJsonString(getCoRespondentSubmitData());

        stubFormatterServerEndpoint(OK, emptyMap(), caseDataString);

        final CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).state(AOS_AWAITING).build();
        stubMaintenanceServerEndpointForAosRetrieval(OK, convertObjectToJsonString(caseDetails));

        stubMaintenanceServerEndpointForAosUpdate(OK, getCoRespondentSubmitData(), caseDataString);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(emptyMap()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(caseDataString));
    }

    @Test
    public void dueDateIsRecalculatedWhenCoRespondentIsDefending() throws Exception {
        final Map<String, Object> originalSubmissionData = new HashMap<>();
        originalSubmissionData.putAll(getCoRespondentSubmitData());
        originalSubmissionData.put(CO_RESPONDENT_DEFENDS_DIVORCE, "YES");
        originalSubmissionData.put(CO_RESPONDENT_DUE_DATE, "01-01-2001");

        final String caseDataString = convertObjectToJsonString(originalSubmissionData);

        stubFormatterServerEndpoint(OK, originalSubmissionData, caseDataString);

        final CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).state(AOS_AWAITING).build();
        stubMaintenanceServerEndpointForAosRetrieval(OK, convertObjectToJsonString(caseDetails));

        final Map<String, Object> recalculatedSubmissionData = new HashMap<>();
        recalculatedSubmissionData.putAll(getCoRespondentSubmitData());
        recalculatedSubmissionData.put(CO_RESPONDENT_DEFENDS_DIVORCE, "YES");
        recalculatedSubmissionData.put(CO_RESPONDENT_DUE_DATE, today.plusDays(21).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        stubMaintenanceServerEndpointForAosUpdate(OK, recalculatedSubmissionData, caseDataString);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(originalSubmissionData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(caseDataString));
    }

    private Map<String, Object> getCoRespondentSubmitData() {
        return ImmutableMap.of(
            RECEIVED_AOS_FROM_CO_RESP, YES_VALUE,
            RECEIVED_AOS_FROM_CO_RESP_DATE, today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );
    }

    private void stubFormatterServerEndpoint(HttpStatus status, Map<String, Object> caseData, String response) {
        formatterServiceServer.stubFor(post(FORMAT_TO_AOS_CASE_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(caseData)))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(response)));
    }

    private void stubMaintenanceServerEndpointForAosRetrieval(final HttpStatus status, final String response) {
        maintenanceServiceServer.stubFor(get(RETRIEVE_CASE_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(response)));
    }

    private void stubMaintenanceServerEndpointForAosUpdate(HttpStatus status, Map<String, Object> caseData, String response) {
        maintenanceServiceServer.stubFor(post(UPDATE_CONTEXT_PATH + CO_RESPONDENT_SUBMISSION_AOS_AWAITING_EVENT_ID)
            .withRequestBody(equalToJson(convertObjectToJsonString(caseData)))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(response)));
    }
}
