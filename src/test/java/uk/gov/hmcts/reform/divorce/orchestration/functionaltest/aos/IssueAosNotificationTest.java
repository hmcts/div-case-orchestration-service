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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.D8_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ORGANISATION_POLICY_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ORGANISATION_POLICY_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_REASON_UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_WELSH_FEMALE_GENDER_IN_RELATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EmailConstants.RESPONDENT_SOLICITOR_ORGANISATION;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_SOLICITOR_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.GENERIC_UPDATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.PETITIONER_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_PETITIONER_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_RESPONDENT_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.SendPetitionerNoticeOfProceedingsEmailTask.EVENT_ISSUE_AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.SendPetitionerNoticeOfProceedingsEmailTask.EVENT_ISSUE_AOS_FROM_REISSUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class IssueAosNotificationTest extends MockedFunctionalTest {
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
        solDnRejectFeatureToggleOn();
        respSolFeatureToggleOn();
    }

    @Test
    public void givenCorrectPetitionerSolicitorDetails_WithIssueAosEventId_ThenOkResponse() throws Exception {
        addPetitionerSolicitorTestData();
        testTemplateVars.remove(NOTIFICATION_EMAIL);

        runTestProcedureUsing(EVENT_ISSUE_AOS, TEST_USER_EMAIL, SOL_PETITIONER_NOTICE_OF_PROCEEDINGS);
    }

    @Test
    public void givenCorrectPetitionerSolicitorDetails_WithIssueAosFromReIssueEventId_ThenOkResponse() throws Exception {
        addPetitionerSolicitorTestData();
        testTemplateVars.remove(NOTIFICATION_EMAIL);

        runTestProcedureUsing(EVENT_ISSUE_AOS_FROM_REISSUE, TEST_USER_EMAIL, SOL_PETITIONER_NOTICE_OF_PROCEEDINGS);
    }

    @Test
    public void givenCorrectPetitionerDetails_WithIssueAosEventIdAndToggleOn_ThenOkResponse() throws Exception {
        addPetitionerTestDataForNoticeOfProceeding();

        runTestProcedureUsing(EVENT_ISSUE_AOS, TEST_USER_EMAIL, PETITIONER_NOTICE_OF_PROCEEDINGS);
    }

    @Test
    public void givenCorrectPetitionerDetails_WithIssueAosEventIdAndToggleOff_ThenOkResponse() throws Exception {
        solDnRejectFeatureToggleOff();
        addPetitionerTestData();

        runTestProcedureUsing(EVENT_ISSUE_AOS, TEST_USER_EMAIL, GENERIC_UPDATE);
    }

    @Test
    public void givenCorrectPetitionerDetails_WithIssueAosFromReIssueEventId_ThenOkResponse() throws Exception {
        addPetitionerTestDataForNoticeOfProceeding();

        runTestProcedureUsing(EVENT_ISSUE_AOS_FROM_REISSUE, TEST_USER_EMAIL, PETITIONER_NOTICE_OF_PROCEEDINGS);
    }

    @Test
    public void givenCorrectRespondentSolicitorDetails_WithIssueAosEventId_ThenOkResponse() throws Exception {
        addPetitionerSolicitorTestData();
        addRespondentSolicitorTestData();
        testTemplateVars.remove(NOTIFICATION_EMAIL);

        setEventIdTo(EVENT_ISSUE_AOS);
        runTestProcedureUsing(SOL_PETITIONER_NOTICE_OF_PROCEEDINGS, TEST_USER_EMAIL,
            SOL_RESPONDENT_NOTICE_OF_PROCEEDINGS, TEST_RESPONDENT_SOLICITOR_EMAIL);
    }

    @Test
    public void givenCorrectRespondentSolicitorDetails_WithIssueAosFromReIssueEventId_ThenOkResponse() throws Exception {
        addPetitionerSolicitorTestData();
        addRespondentSolicitorTestData();
        testTemplateVars.remove(NOTIFICATION_EMAIL);

        setEventIdTo(EVENT_ISSUE_AOS_FROM_REISSUE);
        runTestProcedureUsing(SOL_PETITIONER_NOTICE_OF_PROCEEDINGS, TEST_USER_EMAIL,
            SOL_RESPONDENT_NOTICE_OF_PROCEEDINGS, TEST_RESPONDENT_SOLICITOR_EMAIL);
    }

    @Test
    public void givenCorrectPetitionerAndRespondentSolicitorDetails_WithIssueAosEventIdAndToggleOff_ThenOkResponse() throws Exception {
        solDnRejectFeatureToggleOff();
        addPetitionerTestData();
        addPetitionerSolicitorTestData();
        addRespondentSolicitorTestData();
        testTemplateVars.remove(NOTIFICATION_EMAIL);

        setEventIdTo(EVENT_ISSUE_AOS);
        runTestProcedureUsing(GENERIC_UPDATE, TEST_USER_EMAIL,
            SOL_RESPONDENT_NOTICE_OF_PROCEEDINGS, TEST_RESPONDENT_SOLICITOR_EMAIL);
    }

    private void runTestProcedureUsing(String eventId, String expectedUserEmail, EmailTemplateNames expectedTemplate) throws Exception {
        setEventIdTo(eventId);
        setUpEmailClientMockWith(expectedTemplate.name(), expectedUserEmail, testTemplateVars);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(testData).build();
        expect(status().isOk(), expectedResponse);
        verifySendEmailIsCalledWithUserDataAnd(expectedTemplate.name(), expectedUserEmail);
    }

    private void runTestProcedureUsing(EmailTemplateNames template1, String userEmail1, EmailTemplateNames template2, String userEmail2)
        throws Exception {
        setUpEmailClientMockWith(template1.name(), userEmail1, testTemplateVars);
        setUpEmailClientMockWith(template2.name(), userEmail2, testTemplateVars);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(testData).build();
        expect(status().isOk(), expectedResponse);

        verifySendEmailIsCalledWithUserDataAnd(template1.name(), userEmail1);
        verifySendEmailIsCalledWithUserDataAnd(template2.name(), userEmail2);
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

    private void addPetitionerSolicitorTestData() {
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

    private void addRespondentSolicitorTestData() {
        testData.put(RESPONDENT_SOLICITOR_EMAIL_ADDRESS, TEST_RESPONDENT_SOLICITOR_EMAIL);
        testData.put(RESPONDENT_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        testData.put(RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());

        testTemplateVars.put(RESPONDENT_SOLICITOR_ORGANISATION, TEST_ORGANISATION_POLICY_NAME);
    }

    private void setEventIdTo(String eventId) {
        testEventId = eventId;
    }

    private void setUpEmailClientMockWith(String templateName, String userEmail, Map emailArgs) throws NotificationClientException {
        when(mockEmailClient
            .sendEmail(eq(templateIdOf(templateName)), eq(userEmail), eq(emailArgs), anyString()))
            .thenReturn(null);
    }

    private void verifySendEmailIsCalledWithUserDataAnd(String templateName, String userEmail) throws NotificationClientException {
        verify(mockEmailClient, times(1))
            .sendEmail(eq(templateIdOf(templateName)), eq(userEmail), eq(testTemplateVars), anyString());
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

    private OrganisationPolicy buildOrganisationPolicy() {
        return OrganisationPolicy.builder()
            .organisation(
                Organisation.builder()
                    .organisationID(TEST_ORGANISATION_POLICY_ID)
                    .organisationName(TEST_ORGANISATION_POLICY_NAME)
                    .build())
            .build();
    }

    private void solDnRejectFeatureToggleOff() {
        when(featureToggleService.isFeatureEnabled(Features.SOLICITOR_DN_REJECT_AND_AMEND)).thenReturn(false);
    }

    private void solDnRejectFeatureToggleOn() {
        when(featureToggleService.isFeatureEnabled(Features.SOLICITOR_DN_REJECT_AND_AMEND)).thenReturn(true);
    }

    private void respSolFeatureToggleOff() {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);
    }

    private void respSolFeatureToggleOn() {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);
    }
}
