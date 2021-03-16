package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_PAYMENT_CHEQUE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;



@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
public class MigrateChequePaymentTest extends IdamTestSupport {

    private static final String API_URL = "/migrate-cheque-payment";
    private static final String IDAM_USER_DETAILS_CONTEXT_PATH = "/details";
    private static final String EVENT_ID = "MigrateChequePayment";

    @Autowired
    private MockMvc webClient;

    @Before
    public void setUp() {
        stubSignInForCaseworker();
        stubUserDetailsEndpoint(OK, AUTH_TOKEN, USER_DETAILS_JSON);
    }

    @Test
    public void givenPaymentMethodIsCheque_thenResponseWithNoError() throws Exception {

        final Map<String, Object> caseData = new HashMap<>();
        final Map<String, Object> migratedCaseData = new HashMap<>();

        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, SOL_PAYMENT_CHEQUE_VALUE);
        migratedCaseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(AOS_OVERDUE)
            .caseData(caseData)
            .build();

        CcdCallbackRequest request = CcdCallbackRequest.builder()
            .eventId(EVENT_ID)
            .caseDetails(caseDetails)
            .build();

        maintenanceServiceServer.stubFor(WireMock.request(POST.name(),urlEqualTo(
            "/casemaintenance/version/1/updateCase/test.case.id/MigrateChequePayment"
        )).willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(migratedCaseData))));

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data",
                    is(migratedCaseData)),
                hasJsonPath("$.errors",
                    is(emptyOrNullString())
                )
            )));
    }

    public void stubUserDetailsEndpoint(HttpStatus status, String authHeader, String message) {
        idamServer.stubFor(get(IDAM_USER_DETAILS_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(authHeader))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(message)));
    }
}
