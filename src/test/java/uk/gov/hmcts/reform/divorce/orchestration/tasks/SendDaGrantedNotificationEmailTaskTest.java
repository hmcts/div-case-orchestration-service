package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DECREE_ABSOLUTE_GRANTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_COMPANY;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.UNFORMATTED_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_COMPANY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SendDaGrantedNotificationEmailTaskTest {

    @Mock EmailService emailService;

    @InjectMocks
    SendDaGrantedNotificationEmailTask sendDaGrantedNotificationEmail;

    private static String DA_GRANTED_NOTIFICATION_EMAIL_DESC = "Decree Absolute Notification - Decree Absolute Granted";
    private static String SOL_DA_GRANTED_NOTIFICATION_EMAIL_DESC = "Decree Absolute Notification To Solicitor - Decree Absolute Granted";
    private TaskContext context;
    private Map<String, Object> testData;
    private Map<String, String> expectedPetitionerTemplateVars;
    private Map<String, String> expectedRespondentTemplateVars;
    private Map<String, String> expectedPetSolicitorTemplateVars;
    private Map<String, String> expectedRespSolicitorTemplateVars;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);

        testData = new HashMap<>();
        testData.put(D_8_CASE_REFERENCE, TEST_CASE_ID);
        testData.put(DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD, TEST_DECREE_ABSOLUTE_GRANTED_DATE);

        expectedPetitionerTemplateVars = new HashMap<>();
        expectedPetitionerTemplateVars.put(NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_ID);
        expectedPetitionerTemplateVars.put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_PETITIONER_EMAIL);
        expectedPetitionerTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME);
        expectedPetitionerTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME);
        expectedPetitionerTemplateVars.put(NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE,
                TEST_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE);

        expectedRespondentTemplateVars = new HashMap<>();
        expectedRespondentTemplateVars.put(NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_ID);
        expectedRespondentTemplateVars.put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_RESPONDENT_EMAIL);
        expectedRespondentTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_RESPONDENT_FIRST_NAME);
        expectedRespondentTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_RESPONDENT_LAST_NAME);
        expectedRespondentTemplateVars.put(NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE,
                TEST_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE);

        expectedPetSolicitorTemplateVars = new HashMap<>();
        expectedPetSolicitorTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, UNFORMATTED_CASE_ID);
        expectedPetSolicitorTemplateVars.put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_SOLICITOR_EMAIL);
        expectedPetSolicitorTemplateVars.put(NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        expectedPetSolicitorTemplateVars.put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME);
        expectedPetSolicitorTemplateVars.put(NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FIRST_NAME + " " + TEST_RESPONDENT_LAST_NAME);

        expectedRespSolicitorTemplateVars = new HashMap<>();
        expectedRespSolicitorTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, UNFORMATTED_CASE_ID);
        expectedRespSolicitorTemplateVars.put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_RESP_SOLICITOR_EMAIL);
        expectedRespSolicitorTemplateVars.put(NOTIFICATION_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        expectedRespSolicitorTemplateVars.put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME);
        expectedRespSolicitorTemplateVars.put(NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FIRST_NAME + " " + TEST_RESPONDENT_LAST_NAME);
    }

    @Test
    public void shouldNotCallEmailServiceForDaNotificationIfEmailsDoNotExist() {
        testData.put(D_8_PETITIONER_EMAIL, "");
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(RESPONDENT_EMAIL_ADDRESS, "");
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);

        try {
            sendDaGrantedNotificationEmail.execute(context, testData);
            fail("Failed to throw task exception");
        } catch (TaskException e) {
            verifyZeroInteractions(emailService);
            assertThat(e.getMessage(), is(format("Could not evaluate value of mandatory property \"%s\"", "D8PetitionerEmail")));
        }
    }

    @Test
    public void shouldCallEmailServiceForDaNotificationIfEmailsOnlyOnceIfOnlyOneEmailIsPresent() throws NotificationClientException {
        testData.put(CASE_ID_JSON_KEY, TEST_CASE_ID);
        testData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(RESPONDENT_EMAIL_ADDRESS, "");
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        testData.put(NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE, TEST_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE);

        try {
            sendDaGrantedNotificationEmail.execute(context, testData);
            fail("Failed to throw task exception");
        } catch (TaskException e) {
            verify(emailService,times(1))
                    .sendEmailAndReturnExceptionIfFails(
                            eq(TEST_PETITIONER_EMAIL),
                            eq(EmailTemplateNames.DA_GRANTED_NOTIFICATION.name()),
                            eq(expectedPetitionerTemplateVars),
                            eq(DA_GRANTED_NOTIFICATION_EMAIL_DESC));

            verifyNoMoreInteractions(emailService);
            assertThat(e.getMessage(), is(format("Could not evaluate value of mandatory property \"%s\"", "RespEmailAddress")));
        }
    }

    @Test
    public void shouldCallEmailServiceForDaNotificationEmails() throws TaskException {
        testData.put(CASE_ID_JSON_KEY, TEST_CASE_ID);
        testData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        testData.put(NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE, TEST_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE);

        Map returnPayload = sendDaGrantedNotificationEmail.execute(context, testData);

        assertEquals(testData, returnPayload);

        try {
            verify(emailService)
                    .sendEmailAndReturnExceptionIfFails(
                            eq(TEST_PETITIONER_EMAIL),
                            eq(EmailTemplateNames.DA_GRANTED_NOTIFICATION.name()),
                            eq(expectedPetitionerTemplateVars),
                            eq(DA_GRANTED_NOTIFICATION_EMAIL_DESC));

            verify(emailService)
                    .sendEmailAndReturnExceptionIfFails(
                            eq(TEST_RESPONDENT_EMAIL),
                            eq(EmailTemplateNames.DA_GRANTED_NOTIFICATION.name()),
                            eq(expectedRespondentTemplateVars),
                            eq(DA_GRANTED_NOTIFICATION_EMAIL_DESC));
        } catch (NotificationClientException e) {
            fail("Failed to throw task exception");
        }
    }

    @Test
    public void shouldCallEmailServiceForDaNotificationIfSolicitorIsRepresentingPetitioner() throws TaskException {
        testData.put(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
        testData.put(PET_SOL_EMAIL, TEST_SOLICITOR_EMAIL);
        testData.put(PET_SOL_NAME, TEST_SOLICITOR_NAME);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);

        Map returnPayload = sendDaGrantedNotificationEmail.execute(context, testData);

        assertEquals(testData, returnPayload);

        try {
            verify(emailService)
                    .sendEmail(
                            eq(TEST_SOLICITOR_EMAIL),
                            eq(EmailTemplateNames.SOL_DA_GRANTED_NOTIFICATION.name()),
                            eq(expectedPetSolicitorTemplateVars),
                            eq(SOL_DA_GRANTED_NOTIFICATION_EMAIL_DESC));

            verify(emailService)
                    .sendEmailAndReturnExceptionIfFails(
                            eq(TEST_RESPONDENT_EMAIL),
                            eq(EmailTemplateNames.DA_GRANTED_NOTIFICATION.name()),
                            eq(expectedRespondentTemplateVars),
                            eq(DA_GRANTED_NOTIFICATION_EMAIL_DESC));
        } catch (NotificationClientException e) {
            fail("Failed to throw task exception");
        }
    }

    // test until we implement setting respondentSolicitorRepresented from CCD for RespSols
    @Test
    public void shouldCallEmailServiceForDaNotificationIfSolicitorIsRepresentingRespAndRespSolRepresentedValueIsNotPresent() throws TaskException {
        testData.put(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
        testData.put(D8_RESPONDENT_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        testData.put(D8_RESPONDENT_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        testData.put(D8_RESPONDENT_SOLICITOR_COMPANY, TEST_SOLICITOR_COMPANY);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);

        Map returnPayload = sendDaGrantedNotificationEmail.execute(context, testData);

        assertEquals(testData, returnPayload);

        try {
            verify(emailService)
                    .sendEmailAndReturnExceptionIfFails(
                            eq(TEST_PETITIONER_EMAIL),
                            eq(EmailTemplateNames.DA_GRANTED_NOTIFICATION.name()),
                            eq(expectedPetitionerTemplateVars),
                            eq(DA_GRANTED_NOTIFICATION_EMAIL_DESC));

            verify(emailService)
                    .sendEmail(
                            eq(TEST_RESP_SOLICITOR_EMAIL),
                            eq(EmailTemplateNames.SOL_DA_GRANTED_NOTIFICATION.name()),
                            eq(expectedRespSolicitorTemplateVars),
                            eq(SOL_DA_GRANTED_NOTIFICATION_EMAIL_DESC));
        } catch (NotificationClientException e) {
            fail("Failed to throw task exception");
        }
    }

    @Test
    public void shouldCallEmailServiceForDaNotificationIfSolicitorIsRepresentingRespAndRespSolRepresentedValueIsPresent() throws TaskException {
        testData.put(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
        testData.put(D8_RESPONDENT_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        testData.put(D8_RESPONDENT_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        testData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);

        Map returnPayload = sendDaGrantedNotificationEmail.execute(context, testData);

        assertEquals(testData, returnPayload);

        try {
            verify(emailService)
                    .sendEmailAndReturnExceptionIfFails(
                            eq(TEST_PETITIONER_EMAIL),
                            eq(EmailTemplateNames.DA_GRANTED_NOTIFICATION.name()),
                            eq(expectedPetitionerTemplateVars),
                            eq(DA_GRANTED_NOTIFICATION_EMAIL_DESC));

            verify(emailService)
                    .sendEmail(
                            eq(TEST_RESP_SOLICITOR_EMAIL),
                            eq(EmailTemplateNames.SOL_DA_GRANTED_NOTIFICATION.name()),
                            eq(expectedRespSolicitorTemplateVars),
                            eq(SOL_DA_GRANTED_NOTIFICATION_EMAIL_DESC));
        } catch (NotificationClientException e) {
            fail("Failed to throw task exception");
        }
    }

    @Test
    public void shouldCallEmailServiceForDaNotificationIfBothPartiesAreRepresentedBySolicitors() throws TaskException {
        testData.put(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
        testData.put(PET_SOL_EMAIL, TEST_SOLICITOR_EMAIL);
        testData.put(PET_SOL_NAME, TEST_SOLICITOR_NAME);
        testData.put(D8_RESPONDENT_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        testData.put(D8_RESPONDENT_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        testData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);

        Map returnPayload = sendDaGrantedNotificationEmail.execute(context, testData);

        assertEquals(testData, returnPayload);

        verify(emailService)
                .sendEmail(
                        eq(TEST_SOLICITOR_EMAIL),
                        eq(EmailTemplateNames.SOL_DA_GRANTED_NOTIFICATION.name()),
                        eq(expectedPetSolicitorTemplateVars),
                        eq(SOL_DA_GRANTED_NOTIFICATION_EMAIL_DESC));

        verify(emailService)
                .sendEmail(
                        eq(TEST_RESP_SOLICITOR_EMAIL),
                        eq(EmailTemplateNames.SOL_DA_GRANTED_NOTIFICATION.name()),
                        eq(expectedRespSolicitorTemplateVars),
                        eq(SOL_DA_GRANTED_NOTIFICATION_EMAIL_DESC));
    }
}