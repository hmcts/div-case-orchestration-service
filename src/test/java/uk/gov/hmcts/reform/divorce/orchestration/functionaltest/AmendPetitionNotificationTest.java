package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INFERRED_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_FEES_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.PETITIONER_AMEND_APPLICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@WebMvcTest
public class AmendPetitionNotificationTest extends MockedFunctionalTest {
    private static final String API_URL = "/amend-application";
    private static final String AMEND_PETITION_FEE_CONTEXT_PATH = "/fees-and-payments/version/1/amend-fee";
    private static String testEventId;
    private static Map<String, Object> testData;
    private static Map<String, Object> testTemplateVars;

    @Autowired
    private MockMvc webClient;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    @MockBean
    EmailClient mockEmailClient;

    @Before
    public void setup() {
        testData = new HashMap<>();
        testTemplateVars = new HashMap<>();
    }

    @Test
    public void givenCorrectPreState_sendAmendPetitionNotification_ThenOkResponse() throws Exception {
        runPetitionerTestWithPreState();
    }

    @Test
    public void givenBadRequestBody_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(mockEmailClient);
    }

    private void runPetitionerTestWithPreState() throws Exception {
        addPetitionerTestData();

        runTestProcedureUsing(PETITIONER_AMEND_APPLICATION);
    }

    private void runTestProcedureUsing(EmailTemplateNames expectedTemplate) throws Exception {
        setEventIdTo(AOS_AWAITING);
        setUpEmailClientMockWith(expectedTemplate.name(), testTemplateVars);

        stubGetFeeFromFeesAndPayments(HttpStatus.OK, FeeResponse.builder().amount(TEST_FEE_AMOUNT).build());

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(testData).build();
        expect(status().isOk(), expectedResponse);
        verifySendEmailIsCalledWithUserDataAnd(expectedTemplate.name());
    }

    private void addPetitionerTestData() {
        testData.put(D_8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(D_8_INFERRED_RESPONDENT_GENDER, TEST_INFERRED_GENDER);

        testTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID);
        testTemplateVars.put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME);
        testTemplateVars.put(NOTIFICATION_FEES_KEY, "550");
        testTemplateVars.put(NOTIFICATION_HUSBAND_OR_WIFE, "wife");
    }

    private void stubGetFeeFromFeesAndPayments(HttpStatus status, FeeResponse feeResponse) {
        feesAndPaymentsServer.stubFor(WireMock.get(AMEND_PETITION_FEE_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(feeResponse))));
    }

    private void setUpEmailClientMockWith(String templateName, Map emailArgs) throws NotificationClientException {
        when(mockEmailClient
            .sendEmail(eq(templateIdOf(templateName)), eq(TEST_USER_EMAIL), eq(emailArgs), anyString()))
            .thenReturn(null);
    }

    private void setEventIdTo(String eventId) {
        testEventId = eventId;
    }

    private void verifySendEmailIsCalledWithUserDataAnd(String templateName) throws NotificationClientException {
        verify(mockEmailClient, times(1))
            .sendEmail(eq(templateIdOf(templateName)), eq(TEST_USER_EMAIL), eq(testTemplateVars), anyString());
    }

    private String templateIdOf(String templateName) {
        return emailTemplatesConfig.getTemplates().get(LanguagePreference.ENGLISH).get(templateName);
    }

    private CaseDetails caseDetailsOf(Map data) {
        return CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(data)
            .build();
    }

    private CcdCallbackRequest requestFrom(CaseDetails caseDetails, String eventId) {
        return CcdCallbackRequest.builder()
            .eventId(eventId)
            .token(TEST_TOKEN)
            .caseDetails(caseDetails)
            .build();
    }

    private void expect(ResultMatcher statusCondition, CcdCallbackResponse expectedResponse) throws Exception {
        CcdCallbackRequest ccdCallbackRequest = requestFrom(caseDetailsOf(testData), testEventId);
        String inputJson = convertObjectToJsonString(ccdCallbackRequest);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(statusCondition)
            .andExpect(MockMvcResultMatchers.content().json(convertObjectToJsonString(expectedResponse)));
    }
}
