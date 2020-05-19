package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.config.TemplateConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.D8_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_REASON_2_YEAR_SEP;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_REASON_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_REASON_UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_WELSH_FEMALE_GENDER_IN_RELATION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.UNFORMATTED_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCED_WHO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOT_RECEIVED_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOT_RECEIVED_AOS_STARTED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SendPetitionerUpdateNotificationEmailTest {

    @Mock
    EmailService emailService;

    @Autowired
    TemplateConfig templateConfig;

    SendPetitionerUpdateNotificationsEmail sendPetitionerUpdateNotificationsEmail;

    private Map<String, Object> testData;
    private TaskContext context;
    private Map<String, String> expectedTemplateVars;


    private static final String RESP_ANSWER_RECVD_EVENT = "answerReceived";
    private static final String RESP_ANSWER_NOT_RECVD_EVENT = "answerNotReceived";

    @Before
    public void setup() {
        sendPetitionerUpdateNotificationsEmail = new SendPetitionerUpdateNotificationsEmail(emailService,
                templateConfig);

        testData = new HashMap<>();

        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);

        expectedTemplateVars = new HashMap<>();
    }

    private void addPetTestData() {
        testData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        testData.put(D_8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(D_8_REASON_FOR_DIVORCE, TEST_REASON_UNREASONABLE_BEHAVIOUR);
        testData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        testData.put(D_8_DIVORCED_WHO, TEST_RELATIONSHIP);

        expectedTemplateVars.put(NOTIFICATION_EMAIL, TEST_USER_EMAIL);
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME);
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME);
        expectedTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, D8_CASE_ID);
        expectedTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, TEST_RELATIONSHIP);
        expectedTemplateVars.put(NOTIFICATION_WELSH_RELATIONSHIP_KEY, TEST_WELSH_FEMALE_GENDER_IN_RELATION);
    }

    private void addSolicitorTestData() {
        testData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        testData.put(PET_SOL_EMAIL, TEST_USER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_USER_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_USER_LAST_NAME);
        testData.put(PET_SOL_NAME, TEST_SOLICITOR_NAME);

        expectedTemplateVars.put(NOTIFICATION_EMAIL, TEST_USER_EMAIL);
        expectedTemplateVars.put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME);
        expectedTemplateVars.put(NOTIFICATION_RESP_NAME, TEST_USER_FIRST_NAME + " " + TEST_USER_LAST_NAME);
        expectedTemplateVars.put(NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        expectedTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, UNFORMATTED_CASE_ID);
    }

    private void setEventIdTo(String eventId) {
        context.setTransientObject(CASE_EVENT_ID_JSON_KEY, eventId);
    }

    private void verifyCallsEmailTemplate(String emailTemplateName) throws Exception {
        testData.put(LANGUAGE_PREFERENCE_WELSH, "No");
        Map returnPayload = sendPetitionerUpdateNotificationsEmail.execute(context, testData);

        assertEquals(testData, returnPayload);

        verify(emailService).sendEmailAndReturnExceptionIfFails(
                eq(TEST_USER_EMAIL),
                eq(emailTemplateName),
                eq(expectedTemplateVars),
                anyString(),
                eq(LanguagePreference.ENGLISH));
    }

    @Test
    public void shouldNotCallEmailServiceForUpdateIfPetitionerOrSolicitorEmailDoesNotExist() throws Exception {
        testData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);

        sendPetitionerUpdateNotificationsEmail.execute(context, testData);

        verifyZeroInteractions(emailService);
    }

    @Test
    public void shouldCallEmailServiceForSolGenericUpdate() throws Exception {
        addSolicitorTestData();

        verifyCallsEmailTemplate(EmailTemplateNames.SOL_GENERAL_CASE_UPDATE.name());
    }

    @Test
    public void shouldCallAppropriateSolEmailServiceWhenAosNotReceived() throws Exception {
        addSolicitorTestData();
        setEventIdTo(NOT_RECEIVED_AOS_EVENT_ID);

        verifyCallsEmailTemplate(EmailTemplateNames.SOL_APPLICANT_RESP_NOT_RESPONDED.name());
    }

    @Test
    public void shouldCallAppropriateSolEmailServiceWhenAosNotReceivedStarted() throws Exception {
        addSolicitorTestData();
        setEventIdTo(NOT_RECEIVED_AOS_STARTED_EVENT_ID);

        verifyCallsEmailTemplate(EmailTemplateNames.SOL_APPLICANT_RESP_NOT_RESPONDED.name());
    }

    @Test
    public void shouldCallAppropriateSolEmailServiceWhenRespNotResponded() throws Exception {
        addSolicitorTestData();
        setEventIdTo(RESP_ANSWER_NOT_RECVD_EVENT);

        verifyCallsEmailTemplate(EmailTemplateNames.SOL_APPLICANT_RESP_NOT_RESPONDED.name());
    }

    @Test
    public void shouldCallAppropriateSolEmailServiceWhenRespAnswerReceived() throws Exception {
        addSolicitorTestData();
        setEventIdTo(RESP_ANSWER_RECVD_EVENT);

        verifyCallsEmailTemplate(EmailTemplateNames.SOL_APPLICANT_AOS_RECEIVED.name());
    }

    @Test
    public void shouldThrowExceptionAndNotSendEmailWhenMandatoryFieldIsMissingForSolicitor() {
        addSolicitorTestData();
        testData.replace(RESP_FIRST_NAME_CCD_FIELD, null);
        try {
            sendPetitionerUpdateNotificationsEmail.execute(context, testData);
            fail("Failed to catch task exception");
        } catch (TaskException e) {
            verifyZeroInteractions(emailService);
            assertThat(e.getMessage(), is(format("Could not evaluate value of mandatory property \"%s\"", "D8RespondentFirstName")));
        }
    }

    @Test
    public void shouldCallEmailServiceForPetGenericUpdate() throws Exception {
        addPetTestData();

        verifyCallsEmailTemplate(EmailTemplateNames.GENERIC_UPDATE.name());
    }

    @Test
    public void shouldCallAppropriatePetEmailServiceWhenAosNotReceived() throws Exception {
        addPetTestData();
        setEventIdTo(NOT_RECEIVED_AOS_EVENT_ID);

        verifyCallsEmailTemplate(EmailTemplateNames.PETITIONER_RESP_NOT_RESPONDED.name());
    }

    @Test
    public void shouldCallAppropriatePetEmailServiceWhenAosNotReceivedStarted() throws Exception {
        addPetTestData();
        setEventIdTo(NOT_RECEIVED_AOS_STARTED_EVENT_ID);

        verifyCallsEmailTemplate(EmailTemplateNames.PETITIONER_RESP_NOT_RESPONDED.name());
    }

    @Test
    public void shouldCallAppropriatePetEmailServiceWhenRespNotResponded() throws Exception {
        addPetTestData();
        setEventIdTo(RESP_ANSWER_NOT_RECVD_EVENT);

        verifyCallsEmailTemplate(EmailTemplateNames.PETITIONER_RESP_NOT_RESPONDED.name());
    }

    @Test
    public void shouldCallAppropriatePetEmailServiceWhenRespDoesNotAdmitAdultery() throws Exception {
        addPetTestData();
        testData.replace(D_8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        testData.replace(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        setEventIdTo(RESP_ANSWER_RECVD_EVENT);

        verifyCallsEmailTemplate(EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY.name());
    }

    @Test
    public void shouldCallAppropriateEmailServiceWhenRespDoesNotAdmitAdulteryCoRespNoReply() throws Exception {
        addPetTestData();
        testData.replace(D_8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        testData.replace(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        testData.put(D_8_CO_RESPONDENT_NAMED, YES_VALUE);
        testData.put(RECEIVED_AOS_FROM_CO_RESP, NO_VALUE);
        setEventIdTo(RESP_ANSWER_RECVD_EVENT);

        verifyCallsEmailTemplate(EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED.name());
    }

    @Test
    public void shouldCallAppropriateEmailServiceWhenRespDoesNotConsentTo2YrsSeparation() throws Exception {
        addPetTestData();
        testData.replace(D_8_REASON_FOR_DIVORCE, TEST_REASON_2_YEAR_SEP);
        testData.replace(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        setEventIdTo(RESP_ANSWER_RECVD_EVENT);

        verifyCallsEmailTemplate(EmailTemplateNames.AOS_RECEIVED_NO_CONSENT_2_YEARS.name());
    }

    @Test
    public void shouldCallEmailServiceWithNoCaseIdFormatWhenNoUnableToFormatIdForGenericUpdate() throws Exception {
        addPetTestData();
        setEventIdTo(RESP_ANSWER_RECVD_EVENT);
        expectedTemplateVars.replace(NOTIFICATION_CCD_REFERENCE_KEY, D8_CASE_ID);

        verifyCallsEmailTemplate(EmailTemplateNames.GENERIC_UPDATE.name());
    }

    @Test
    public void shouldThrowExceptionAndNotSendEmailWhenMandatoryFieldIsMissingForPetitioner() {
        addPetTestData();
        testData.replace(D_8_DIVORCED_WHO, null);
        try {
            sendPetitionerUpdateNotificationsEmail.execute(context, testData);
            fail("Failed to catch task exception");
        } catch (TaskException e) {
            verifyZeroInteractions(emailService);
            assertThat(e.getMessage(), is(format("Could not evaluate value of mandatory property \"%s\"", "D8DivorceWho")));
        }
    }
}
