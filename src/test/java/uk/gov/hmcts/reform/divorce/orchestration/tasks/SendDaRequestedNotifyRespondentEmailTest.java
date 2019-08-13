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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DECREE_ABSOLUTE_GRANTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INFERRED_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;


@RunWith(MockitoJUnitRunner.class)
public class SendDaRequestedNotifyRespondentEmailTest {

    @Mock EmailService emailService;

    @InjectMocks
    SendDaRequestedNotifyRespondentEmail sendDaRequestedNotifyRespondentEmail;

    private static String APPLICANT_REQUESTED_DA_NOTIFICATION_EMAIL_DESC =
        "Decree Absolute Requested Notification - Applicant Requested Decree Absolute";
    private TaskContext context;
    private Map<String, Object> testData;
    private Map<String, String> expectedTemplateVars;

    @Before
    public void setup() {
        context = new DefaultTaskContext();

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

        sendDaRequestedNotifyRespondentEmail.execute(context, testData);
        verifyZeroInteractions(emailService);
    }

    @Test
    public void shouldCallEmailServiceForDaRequestedNotifyRespondentEmail() throws TaskException {
        testData.put(CASE_ID_JSON_KEY, TEST_CASE_ID);
        testData.put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        testData.put(NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP);
        testData.put(D_8_INFERRED_PETITIONER_GENDER, TEST_INFERRED_GENDER);


        Map returnPayload = sendDaRequestedNotifyRespondentEmail.execute(context, testData);

        assertEquals(testData, returnPayload);

        try {
            verify(emailService)
                .sendEmailAndReturnExceptionIfFails(
                    eq(TEST_RESPONDENT_EMAIL),
                    eq(EmailTemplateNames.DECREE_ABSOLUTE_REQUESTED_NOTIFICATION.name()),
                    eq(expectedTemplateVars),
                    eq(APPLICANT_REQUESTED_DA_NOTIFICATION_EMAIL_DESC));
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
            sendDaRequestedNotifyRespondentEmail.execute(context, testData);
            fail("Failed to catch task exception");
        } catch (TaskException e) {
            assertThat(e.getMessage(), is(format("Could not evaluate value of mandatory property \"%s\"", "D8InferredPetitionerGender")));
        }
    }
}
