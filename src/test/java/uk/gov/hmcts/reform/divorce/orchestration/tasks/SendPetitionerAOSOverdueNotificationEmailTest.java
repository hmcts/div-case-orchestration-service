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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCED_WHO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;


@RunWith(MockitoJUnitRunner.class)
public class SendPetitionerAOSOverdueNotificationEmailTest {

    @Mock EmailService emailService;

    @InjectMocks
    SendPetitionerAOSOverdueNotificationEmail sendPetitionerAOSOverdueNotificationEmail;

    private static String AOS_OVERDUE_NOTIFICATION_EMAIL_DESC = "AOS Overdue Notification - Petitioner";
    private TaskContext context;
    private Map<String, Object> testData;
    private Map<String, String> expectedTemplateVars;

    @Before
    public void setup() {
        context = new DefaultTaskContext();

        testData = new HashMap<>();

        expectedTemplateVars = new HashMap<>();
        expectedTemplateVars.put(NOTIFICATION_EMAIL, TEST_PETITIONER_EMAIL);
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_RESPONDENT_FIRST_NAME);
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_RESPONDENT_LAST_NAME);
        expectedTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, TEST_RELATIONSHIP);
    }

    @Test
    public void shouldNotCallEmailServiceForAOSOverdueNotificationIfEmailDoesNotExist() throws TaskException {
        testData.put(D_8_PETITIONER_EMAIL, "");
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        testData.put(D_8_DIVORCED_WHO, TEST_RELATIONSHIP);

        sendPetitionerAOSOverdueNotificationEmail.execute(context, testData);
        verifyZeroInteractions(emailService);
    }

    @Test
    public void shouldCallEmailServiceForAOSOverdueNotifyPetitionerEmail() throws TaskException {
        testData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        testData.put(D_8_DIVORCED_WHO, TEST_RELATIONSHIP);


        Map returnPayload = sendPetitionerAOSOverdueNotificationEmail.execute(context, testData);

        assertEquals(testData, returnPayload);

        try {
            verify(emailService)
                .sendEmailAndReturnExceptionIfFails(
                    eq(TEST_PETITIONER_EMAIL),
                    eq(EmailTemplateNames.APPLICANT_AOS_OVERDUE_NOTIFICATION.name()),
                    eq(expectedTemplateVars),
                    eq(AOS_OVERDUE_NOTIFICATION_EMAIL_DESC));
        } catch (NotificationClientException e) {
            fail("exception occurred in test");
        }
    }

    @Test
    public void shouldThrowExceptionWhenMandatoryFieldIsMissing() {
        testData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        try {
            sendPetitionerAOSOverdueNotificationEmail.execute(context, testData);
            fail("Failed to catch task exception");
        } catch (TaskException e) {
            assertThat(e.getMessage(), is(format("Could not evaluate value of mandatory property \"%s\"", "D8DivorceWho")));
        }
    }
}
