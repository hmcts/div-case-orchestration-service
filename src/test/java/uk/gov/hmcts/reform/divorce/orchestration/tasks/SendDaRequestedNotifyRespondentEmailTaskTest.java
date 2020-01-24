package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DECREE_ABSOLUTE_GRANTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INFERRED_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_SOLICITOR_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaRequestedNotifyRespondentEmailTask.REQUESTED_BY_APPLICANT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaRequestedNotifyRespondentEmailTask.REQUESTED_BY_SOLICITOR;

@RunWith(MockitoJUnitRunner.class)
public class SendDaRequestedNotifyRespondentEmailTaskTest {

    @Mock
    EmailService emailService;

    @InjectMocks
    SendDaRequestedNotifyRespondentEmailTask sendDaRequestedNotifyRespondentEmailTask;

    private TaskContext context;
    private Map<String, Object> testData;
    private Map<String, String> expectedTemplateVars;
    private static final String CCD_CASE_ID = "123123123";

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, CaseDetails.builder().caseId(CCD_CASE_ID).build());

        testData = new HashMap<>();
        testData.put(D_8_CASE_REFERENCE, TEST_CASE_ID);
        testData.put(DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD, TEST_DECREE_ABSOLUTE_GRANTED_DATE);

        expectedTemplateVars = new HashMap<>();
        expectedTemplateVars.put(NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_ID);
        expectedTemplateVars.put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_RESPONDENT_EMAIL);
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_RESPONDENT_FIRST_NAME);
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_RESPONDENT_LAST_NAME);
        expectedTemplateVars.put(NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP);
    }

    @Test
    public void shouldNotCallEmailServiceForDaNotificationIfEmailsDoNotExist() throws TaskException {
        testData.put(RESPONDENT_EMAIL_ADDRESS, "");
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        testData.put(NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP);
        testData.put(D_8_INFERRED_PETITIONER_GENDER, TEST_INFERRED_GENDER);

        sendDaRequestedNotifyRespondentEmailTask.execute(context, testData);
        verifyZeroInteractions(emailService);
    }

    @Test(expected = TaskException.class)
    public void shouldThrowTaskExceptionWhenEmailServiceThrowsException()
        throws TaskException, NotificationClientException {
        testData.put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        testData.put(NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP);
        testData.put(D_8_INFERRED_PETITIONER_GENDER, TEST_INFERRED_GENDER);
        testData.put(LANGUAGE_PREFERENCE_WELSH, "Yes");

        doThrow(new NotificationClientException(new Exception(TEST_ERROR)))
            .when(emailService)
            .sendEmailAndReturnExceptionIfFails(
                eq(TEST_RESPONDENT_EMAIL),
                eq(EmailTemplateNames.DECREE_ABSOLUTE_REQUESTED_NOTIFICATION.name()),
                anyMap(),
                eq(REQUESTED_BY_APPLICANT),
                eq(Optional.of(LanguagePreference.WELSH))

            );

        sendDaRequestedNotifyRespondentEmailTask.execute(context, testData);
    }

    @Test
    public void shouldSendEmailToRespondent() throws TaskException {
        testData.put(CASE_ID_JSON_KEY, TEST_CASE_ID);
        testData.put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        testData.put(NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP);
        testData.put(D_8_INFERRED_PETITIONER_GENDER, TEST_INFERRED_GENDER);
        testData.put(LANGUAGE_PREFERENCE_WELSH, "No");

        Map returnPayload = sendDaRequestedNotifyRespondentEmailTask.execute(context, testData);

        assertEquals(testData, returnPayload);

        try {
            verify(emailService)
                .sendEmailAndReturnExceptionIfFails(
                    eq(TEST_RESPONDENT_EMAIL),
                    eq(EmailTemplateNames.DECREE_ABSOLUTE_REQUESTED_NOTIFICATION.name()),
                    eq(expectedTemplateVars),
                    eq(REQUESTED_BY_APPLICANT),
                    eq(Optional.of(LanguagePreference.ENGLISH)));
        } catch (NotificationClientException e) {
            fail("exception occurred in test");
        }
    }

    @Test
    public void shouldSendEmailToRespondentSolicitor() throws TaskException {
        testData.put(CASE_ID_JSON_KEY, TEST_CASE_ID);
        testData.put(RESPONDENT_EMAIL_ADDRESS, "");
        testData.put(RESPONDENT_SOLICITOR_EMAIL_ADDRESS, TEST_RESPONDENT_SOLICITOR_EMAIL);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_RESPONDENT_LAST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_RELATIONSHIP);
        testData.put(D8_RESPONDENT_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        testData.put(LANGUAGE_PREFERENCE_WELSH, "No");
        Map<String, String> expectedTempVars = prepareExpectedTemplateVarsForSolicitor(testData);

        Map returnPayload = sendDaRequestedNotifyRespondentEmailTask.execute(context, testData);

        assertEquals(testData, returnPayload);

        try {
            verify(emailService)
                .sendEmailAndReturnExceptionIfFails(
                    eq(TEST_RESPONDENT_SOLICITOR_EMAIL),
                    eq(EmailTemplateNames.DECREE_ABSOLUTE_REQUESTED_NOTIFICATION_SOLICITOR.name()),
                    eq(expectedTempVars),
                    eq(REQUESTED_BY_SOLICITOR),
                    eq(Optional.of(LanguagePreference.ENGLISH)));
        } catch (NotificationClientException e) {
            fail("exception occurred in test");
        }
    }

    @Test
    public void shouldThrowExceptionWhenMandatoryFieldIsMissing() {
        testData.put(CASE_ID_JSON_KEY, TEST_CASE_ID);
        testData.put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        testData.put(NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP);

        try {
            sendDaRequestedNotifyRespondentEmailTask.execute(context, testData);
            fail("Failed to catch task exception");
        } catch (TaskException e) {
            assertThat(
                e.getMessage(),
                is(format("Could not evaluate value of mandatory property \"%s\"", "D8InferredPetitionerGender"))
            );
        }
    }

    private Map<String, String> prepareExpectedTemplateVarsForSolicitor(Map<String, Object> caseData) {
        Map<String, String> templateVars = new HashMap<>();

        String respondentName = String
            .format("%s %s", caseData.get(RESP_FIRST_NAME_CCD_FIELD), caseData.get(RESP_LAST_NAME_CCD_FIELD));
        String petitionerName = String
            .format("%s %s", caseData.get(D_8_PETITIONER_FIRST_NAME), caseData.get(D_8_PETITIONER_LAST_NAME));

        templateVars.put(NOTIFICATION_PET_NAME, petitionerName);
        templateVars.put(NOTIFICATION_RESP_NAME, respondentName);
        templateVars.put(NOTIFICATION_SOLICITOR_NAME, (String) caseData.get(D8_RESPONDENT_SOLICITOR_NAME));
        templateVars.put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_RESPONDENT_SOLICITOR_EMAIL);
        templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, CCD_CASE_ID);


        return templateVars;
    }
}
