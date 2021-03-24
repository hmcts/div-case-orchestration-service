package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.callback;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.D8_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_WELSH_FEMALE_GENDER_IN_RELATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.ISSUE_AOS_OFFLINE_RESPONDENT_FROM_AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCED_WHO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class AosPackIssuedCallbackTest extends MockedFunctionalTest {

    private static final String TEST_CMS_UPDATE_URL =
        "/casemaintenance/version/1/updateCase/" + TEST_CASE_ID + "/" + ISSUE_AOS_OFFLINE_RESPONDENT_FROM_AOS_AWAITING;

    private static final String GENERIC_UPDATE_TEMPLATE_ID = "6ee6ec29-5e88-4516-99cb-2edc30256575";

    @MockBean
    private AuthUtil authUtil;

    @MockBean
    private EmailClient emailClient;

    @Autowired
    private MockMvc webClient;

    @Before
    public void setUp() {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
        maintenanceServiceServer.stubFor(
            WireMock.post(urlPathMatching(TEST_CMS_UPDATE_URL))
                .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
                .willReturn(ok())
        );
    }

    @Test
    public void shouldTriggerAosOfflineEventAsynchronously_WhenRespondentIsRepresentedButSolicitorIsNotDigital() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(Map.of(
                RESP_SOL_REPRESENTED, YES_VALUE,
                D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME,
                D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME,
                D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL,
                D_8_DIVORCED_WHO, TEST_RELATIONSHIP,
                D_8_CASE_REFERENCE, D8_CASE_ID
            ))
            .build();
        CcdCallbackRequest payload = CcdCallbackRequest.builder()
            .eventId(TEST_EVENT_ID)
            .caseDetails(caseDetails)
            .build();

        webClient.perform(post("/aos-pack-issued")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(payload))
        ).andExpect(status().isOk());

        verify(emailClient).sendEmail(
            eq(GENERIC_UPDATE_TEMPLATE_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(expectedEmailVars()),
            any()
        );

        await().untilAsserted(() -> {
            maintenanceServiceServer.verify(
                postRequestedFor(urlEqualTo(TEST_CMS_UPDATE_URL))
                    .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTH_TOKEN))
                    .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
            );
        });
    }

    private static Map<String, String> expectedEmailVars() {
        Map<String, String> vars = new HashMap<>();
        vars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME);
        vars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME);
        vars.put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_PETITIONER_EMAIL);
        vars.put(NOTIFICATION_WELSH_RELATIONSHIP_KEY, TEST_WELSH_FEMALE_GENDER_IN_RELATION);
        vars.put(NOTIFICATION_RELATIONSHIP_KEY, TEST_RELATIONSHIP);
        vars.put(NOTIFICATION_CCD_REFERENCE_KEY, D8_CASE_ID);

        return vars;
    }
}
