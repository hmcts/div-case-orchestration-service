package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.aos;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.D8_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_REASON_UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_WELSH_FEMALE_GENDER_IN_RELATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.ISSUE_AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.ISSUE_AOS_FROM_REISSUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.NOT_RECEIVED_AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.NOT_RECEIVED_AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCED_WHO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.GENERIC_UPDATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.PETITIONER_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.PETITIONER_RESP_NOT_RESPONDED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_APPLICANT_RESP_NOT_RESPONDED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_PETITIONER_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class AosOverdueNotificationTest extends MockedFunctionalTest {
    private static final String API_URL = "/petition-updated";
    private static String testEventId;
    private static Map<String, Object> testData;
    private static Map<String, Object> testTemplateVars;

    @Autowired
    private MockMvc webClient;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    @MockBean
    private EmailClient mockEmailClient;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Before
    public void setup() {
        testData = new HashMap<>();
        testTemplateVars = new HashMap<>();
        featureToggleOn();
    }

    @Test
    public void givenCorrectPetitionerDetails_WithAosNotReceivedEventId_ThenOkResponse() throws Exception {
        runPetitionerTestProcedureUsing(NOT_RECEIVED_AOS);
    }

    @Test
    public void givenCorrectPetitionerDetails_WithAosNotReceivedStartedEventId_ThenOkResponse() throws Exception {
        runPetitionerTestProcedureUsing(NOT_RECEIVED_AOS_STARTED);
    }

    @Test
    public void givenCorrectSolicitorDetails_WithAosNotReceivedEventId_ThenOkResponse() throws Exception {
        runSolicitorTestProcedureUsing(NOT_RECEIVED_AOS);
    }

    @Test
    public void givenCorrectSolicitorDetails_WithAosNotReceivedStartedEventId_ThenOkResponse() throws Exception {
        runSolicitorTestProcedureUsing(NOT_RECEIVED_AOS_STARTED);
    }

    @Test
    public void givenCorrectSolicitorDetails_WithIssueAosEventId_ThenOkResponse() throws Exception {
        addSolicitorTestData();
        testTemplateVars.remove(NOTIFICATION_EMAIL);

        runTestProcedureUsing(ISSUE_AOS, SOL_PETITIONER_NOTICE_OF_PROCEEDINGS);
    }

    @Test
    public void givenCorrectSolicitorDetails_WithIssueAosFromReIssueEventId_ThenOkResponse() throws Exception {
        addSolicitorTestData();
        testTemplateVars.remove(NOTIFICATION_EMAIL);

        runTestProcedureUsing(ISSUE_AOS_FROM_REISSUE, SOL_PETITIONER_NOTICE_OF_PROCEEDINGS);
    }

    @Test
    public void givenCorrectPetitionerDetails_WithIssueAosEventIdAndToggleOn_ThenOkResponse() throws Exception {
        addPetitionerTestDataForNoticeOfProceeding();

        runTestProcedureUsing(ISSUE_AOS, PETITIONER_NOTICE_OF_PROCEEDINGS);
    }

    @Test
    public void givenCorrectPetitionerDetails_WithIssueAosEventIdAndToggleOff_ThenOkResponse() throws Exception {
        featureToggleOff();
        addPetitionerTestData();

        runTestProcedureUsing(ISSUE_AOS, GENERIC_UPDATE);
    }

    @Test
    public void givenCorrectPetitionerDetails_WithIssueAosFromReIssueEventId_ThenOkResponse() throws Exception {
        addPetitionerTestDataForNoticeOfProceeding();

        runTestProcedureUsing(ISSUE_AOS_FROM_REISSUE, PETITIONER_NOTICE_OF_PROCEEDINGS);
    }

    @Test
    public void givenBadRequestBody_thenReturnBadRequest() throws Exception {
        setEventIdTo(NOT_RECEIVED_AOS);
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(mockEmailClient);
    }

    @Test
    public void givenEmailServiceThrowsException_ThenInternalServerErrorResponse() throws Exception {
        addPetitionerTestData();
        setEventIdTo(NOT_RECEIVED_AOS);
        String templateName = PETITIONER_RESP_NOT_RESPONDED.name();
        setUpEmailClientMockThrowsExceptionWith(templateName, testTemplateVars);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().errors(Collections.singletonList("test exception")).build();
        expect(status().isOk(), expectedResponse);
        verifySendEmailIsCalledWithUserDataAnd(templateName);
    }

    @Test
    public void givenEmailServiceThrowsExceptionWithSolicitorData_ThenInternalServerErrorResponse() throws Exception {
        addSolicitorTestData();
        setEventIdTo(NOT_RECEIVED_AOS);
        String templateName = SOL_APPLICANT_RESP_NOT_RESPONDED.name();
        setUpEmailClientMockThrowsExceptionWith(templateName, testTemplateVars);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().errors(Collections.singletonList("test exception")).build();
        expect(status().isOk(), expectedResponse);
        verifySendEmailIsCalledWithUserDataAnd(templateName);
    }

    private void runPetitionerTestProcedureUsing(String eventId) throws Exception {
        addPetitionerTestData();

        runTestProcedureUsing(eventId, PETITIONER_RESP_NOT_RESPONDED);
    }

    private void runSolicitorTestProcedureUsing(String eventId) throws Exception {
        addSolicitorTestData();

        runTestProcedureUsing(eventId, SOL_APPLICANT_RESP_NOT_RESPONDED);
    }

    private void runTestProcedureUsing(String eventId, EmailTemplateNames expectedTemplate) throws Exception {
        setEventIdTo(eventId);
        setUpEmailClientMockWith(expectedTemplate.name(), testTemplateVars);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(testData).build();
        expect(status().isOk(), expectedResponse);
        verifySendEmailIsCalledWithUserDataAnd(expectedTemplate.name());
    }

    private void addPetitionerTestData() {
        testData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        testData.put(D_8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        testData.put(D_8_REASON_FOR_DIVORCE, TEST_REASON_UNREASONABLE_BEHAVIOUR);
        testData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        testData.put(D_8_DIVORCED_WHO, TEST_RELATIONSHIP);

        testTemplateVars.put(NOTIFICATION_EMAIL, TEST_USER_EMAIL);
        testTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME);
        testTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME);
        testTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, TEST_RELATIONSHIP);
        testTemplateVars.put(NOTIFICATION_WELSH_RELATIONSHIP_KEY, TEST_WELSH_FEMALE_GENDER_IN_RELATION);
        testTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, D8_CASE_ID);
    }

    private void addPetitionerTestDataForNoticeOfProceeding() {
        testData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        testData.put(D_8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);

        testTemplateVars.put(NOTIFICATION_CASE_NUMBER_KEY, D8_CASE_ID);
        testTemplateVars.put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME);
        testTemplateVars.put(NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FIRST_NAME + " " + TEST_RESPONDENT_LAST_NAME);
    }

    private void addSolicitorTestData() {
        testData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        testData.put(PETITIONER_SOLICITOR_EMAIL, TEST_USER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_USER_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_USER_LAST_NAME);
        testData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);

        testTemplateVars.put(NOTIFICATION_EMAIL, TEST_USER_EMAIL);
        testTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID);
        testTemplateVars.put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME);
        testTemplateVars.put(NOTIFICATION_RESP_NAME, TEST_USER_FIRST_NAME + " " + TEST_USER_LAST_NAME);
        testTemplateVars.put(NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
    }

    private void setEventIdTo(String eventId) {
        testEventId = eventId;
    }

    private void setUpEmailClientMockWith(String templateName, Map emailArgs) throws NotificationClientException {
        when(mockEmailClient
            .sendEmail(eq(templateIdOf(templateName)), eq(TEST_USER_EMAIL), eq(emailArgs), anyString()))
            .thenReturn(null);
    }

    private void setUpEmailClientMockThrowsExceptionWith(String templateName, Map emailArgs) throws NotificationClientException {
        NotificationClientException exception = new NotificationClientException("test exception");
        when(mockEmailClient
            .sendEmail(eq(templateIdOf(templateName)), eq(TEST_USER_EMAIL), eq(emailArgs), anyString()))
            .thenThrow(exception);
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

    private void featureToggleOff() {
        when(featureToggleService.isFeatureEnabled(Features.SOLICITOR_DN_REJECT_AND_AMEND)).thenReturn(false);
    }

    private void featureToggleOn() {
        when(featureToggleService.isFeatureEnabled(Features.SOLICITOR_DN_REJECT_AND_AMEND)).thenReturn(true);
    }
}
