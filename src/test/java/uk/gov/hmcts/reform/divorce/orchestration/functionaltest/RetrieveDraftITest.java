package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_DRAFT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class RetrieveDraftITest extends MockedFunctionalTest {
    private static final String API_URL = "/draftsapi/version/1";
    private static final String CMS_CONTEXT_PATH = "/casemaintenance/version/1/retrieveCase";
    private static final String CMS_UPDATE_CASE_PATH =
        "/casemaintenance/version/1/updateCase/1547073120300616/paymentMade";
    private static final String CFS_CONTEXT_PATH = "/caseformatter/version/1/to-divorce-format";
    private static final String CFS_TO_CCD_CONTEXT_PATH = "/caseformatter/version/1/to-ccd-format";
    private static final String AUTH_SERVICE_PATH = "/lease";

    private static final String CARD_PAYMENT_PATH = "/card-payments/RC-1547-0733-1813-9545";
    private static final String USER_TOKEN = "Some JWT Token";
    private static final String CASE_ID = "12345";

    private static final Map<String, Object> CASE_DATA = new HashMap<>();
    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
        .caseData(CASE_DATA)
        .caseId(CASE_ID)
        .build();

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenJWTTokenIsNull_whenRetrieveDraft_thenReturnBadRequest()
        throws Exception {
        webClient.perform(get(API_URL)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenThereIsAConnectionError_whenRetrieveDraft_thenReturnBadGateway()
        throws Exception {
        final String errorMessage = "some error message";

        stubCmsServerEndpoint(CMS_CONTEXT_PATH, HttpStatus.BAD_GATEWAY, errorMessage, HttpMethod.GET);

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadGateway())
            .andExpect(content().string(containsString(errorMessage)));
    }

    @Test
    public void givenNoDraftInDraftStore_whenRetrieveDraft_thenReturnNotFound()
        throws Exception {

        stubCmsServerEndpoint(CMS_CONTEXT_PATH, HttpStatus.OK, convertObjectToJsonString(CASE_DETAILS), HttpMethod.GET);

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
    }

    @Test
    public void givenEverythingWorksAsExpected_whenCmsCalled_thenReturnDraft()
        throws Exception {

        CASE_DATA.put("deaftProperty1", "value1");
        CASE_DATA.put("deaftProperty2", "value2");
        CASE_DATA.put(IS_DRAFT_KEY, true);

        CaseDetails caseDetails = CaseDetails.builder().caseData(CASE_DATA).build();

        stubCmsServerEndpoint(CMS_CONTEXT_PATH, HttpStatus.OK, convertObjectToJsonString(caseDetails), HttpMethod.GET);
        stubCfsServerEndpoint(convertObjectToJsonString(CASE_DATA));

        Map<String, Object> expectedResponse = Maps.newHashMap(CASE_DATA);
        expectedResponse.put("fetchedDraft", true);

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

    @Test
    public void givenCaseWithCaseId_whenCmsCalled_thenReturnCase() throws Exception {

        CASE_DATA.put("deaftProperty1", "value1");
        CASE_DATA.put("deaftProperty2", "value2");
        CASE_DATA.put(IS_DRAFT_KEY, true);
        stubCmsServerEndpoint(CMS_CONTEXT_PATH, HttpStatus.OK, convertObjectToJsonString(CASE_DETAILS), HttpMethod.GET);
        stubCfsServerEndpoint(convertObjectToJsonString(CASE_DATA));

        Map<String, Object> expectedResponse = Maps.newHashMap(CASE_DATA);

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

    @Test
    public void givenPaidCaseAwaitingPaymentInState_whenCmsCalled_thenReturnUpdateCase() throws Exception {
        String caseDetailsPath = "jsonExamples/payloads/paymentPendingDraft.json";
        String caseDetails = ResourceLoader.loadResourceAsString(caseDetailsPath);

        stubCmsServerEndpoint(CMS_CONTEXT_PATH, HttpStatus.OK, caseDetails, HttpMethod.GET);
        stubCfsServerEndpoint(caseDetails);
        stubAuthServerEndpoint();

        String paymentPath = "jsonExamples/payloads/paymentSystemPaid.json";
        String paymentResponse = ResourceLoader.loadResourceAsString(paymentPath);
        stubPaymentServerEndpoint(paymentResponse);

        String formattedPaymentPath = "jsonExamples/payloads/formattedPayment.json";
        String formattedPayment = ResourceLoader.loadResourceAsString(formattedPaymentPath);
        stubCfsToCCDServerEndpoint(formattedPayment);
        stubCmsServerEndpoint(CMS_UPDATE_CASE_PATH, HttpStatus.OK, caseDetails, HttpMethod.POST);

        Map<String, Object> expectedResponse = Maps.newHashMap(CASE_DATA);

        webClient.perform(get(API_URL)
            .header(AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
        maintenanceServiceServer.verify(2, getRequestedFor(urlEqualTo(CMS_CONTEXT_PATH)));
        maintenanceServiceServer.verify(1, postRequestedFor(urlEqualTo(CMS_UPDATE_CASE_PATH)));

    }

    private void stubCmsServerEndpoint(String path, HttpStatus status, String body, HttpMethod method) {
        maintenanceServiceServer.stubFor(WireMock.request(method.name(), urlEqualTo(path))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(body)));
    }

    private void stubCfsServerEndpoint(String body) {
        formatterServiceServer.stubFor(WireMock.post(CFS_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(body)));
    }

    private void stubCfsToCCDServerEndpoint(String body) {
        formatterServiceServer.stubFor(WireMock.post(CFS_TO_CCD_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(body)));
    }


    private void stubAuthServerEndpoint() {
        Algorithm algorithm = mock(Algorithm.class);
        String body = JWT.create()
            .withExpiresAt(Date.from(ZonedDateTime.now().plusHours(1).toInstant())).sign(algorithm);
        serviceAuthProviderServer.stubFor(WireMock.post(AUTH_SERVICE_PATH)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(body)));
    }

    private void stubPaymentServerEndpoint(String body) {
        paymentServiceServer.stubFor(WireMock.get(CARD_PAYMENT_PATH)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(body)));
    }

}
