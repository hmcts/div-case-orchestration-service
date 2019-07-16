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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.D8_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DECREE_ABSOLUTE_GRANTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.UNFORMATTED_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SendDaGrantedNotificationEmailTest {

    @Mock EmailService emailService;

    @InjectMocks
    SendDaGrantedNotificationEmail sendDaGrantedNotificationEmail;

    private static String DA_GRANTED_NOTIFICATION_EMAIL_DESC = "Decree Absolute Notification - Decree Absolute Granted";
    private TaskContext context;
    private Map<String, Object> testData;
    private Map<String, String> expectedPetitionerTemplateVars;
    private Map<String, String> expectedRespondentTemplateVars;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);

        testData = new HashMap<>();
        testData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        testData.put(DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD, TEST_DECREE_ABSOLUTE_GRANTED_DATE);

        expectedPetitionerTemplateVars = new HashMap<>();
        expectedPetitionerTemplateVars.put(NOTIFICATION_CASE_NUMBER_KEY, D8_CASE_ID);
        expectedPetitionerTemplateVars.put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_PETITIONER_EMAIL);
        expectedPetitionerTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME);
        expectedPetitionerTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME);
        expectedPetitionerTemplateVars.put(NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE,
            TEST_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE);

        expectedRespondentTemplateVars = new HashMap<>();
        expectedRespondentTemplateVars.put(NOTIFICATION_CASE_NUMBER_KEY, D8_CASE_ID);
        expectedRespondentTemplateVars.put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_RESPONDENT_EMAIL);
        expectedRespondentTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_RESPONDENT_FIRST_NAME);
        expectedRespondentTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_RESPONDENT_LAST_NAME);
        expectedRespondentTemplateVars.put(NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE,
            TEST_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE);
    }

    @Test
    public void shouldNotCallEmailServiceForDaNotificationIfEmailsDoNotExist() throws TaskException {
        testData.put(D_8_PETITIONER_EMAIL, "");
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(RESPONDENT_EMAIL_ADDRESS, "");
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);

        sendDaGrantedNotificationEmail.execute(context, testData);

        verifyZeroInteractions(emailService);
    }

    @Test
    public void shouldCallEmailServiceForDaNotificationIfEmailsOnlyOnceIfOnlyOneEmailIsPresent() throws TaskException {
        testData.put(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
        testData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(RESPONDENT_EMAIL_ADDRESS, "");
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        testData.put(NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE, TEST_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE);

        Map returnPayload = sendDaGrantedNotificationEmail.execute(context, testData);

        assertEquals(testData, returnPayload);

        verify(emailService,times(1))
            .sendEmail(
                eq(TEST_PETITIONER_EMAIL),
                eq(EmailTemplateNames.DA_GRANTED_NOTIFICATION.name()),
                eq(expectedPetitionerTemplateVars),
                eq(DA_GRANTED_NOTIFICATION_EMAIL_DESC));

        verifyNoMoreInteractions(emailService);
    }

    @Test
    public void shouldCallEmailServiceForDaNotificationEmails() throws TaskException {
        testData.put(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
        testData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        testData.put(NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE, TEST_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE);

        Map returnPayload = sendDaGrantedNotificationEmail.execute(context, testData);

        assertEquals(testData, returnPayload);

        verify(emailService)
            .sendEmail(
                eq(TEST_PETITIONER_EMAIL),
                eq(EmailTemplateNames.DA_GRANTED_NOTIFICATION.name()),
                eq(expectedPetitionerTemplateVars),
                eq(DA_GRANTED_NOTIFICATION_EMAIL_DESC));

        verify(emailService)
            .sendEmail(
                eq(TEST_RESPONDENT_EMAIL),
                eq(EmailTemplateNames.DA_GRANTED_NOTIFICATION.name()),
                eq(expectedRespondentTemplateVars),
                eq(DA_GRANTED_NOTIFICATION_EMAIL_DESC));
    }
}
