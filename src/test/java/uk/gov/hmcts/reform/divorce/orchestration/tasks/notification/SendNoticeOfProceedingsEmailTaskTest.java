package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_D8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.ISSUE_AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.SendNoticeOfProceedingsEmailTask.EVENT_ISSUE_AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.SendNoticeOfProceedingsEmailTask.EVENT_ISSUE_AOS_FROM_REISSUE;

@RunWith(MockitoJUnitRunner.class)
public class SendNoticeOfProceedingsEmailTaskTest {

    private static final String SOLICITOR_TEMPLATE = EmailTemplateNames.SOL_PETITIONER_NOTICE_OF_PROCEEDINGS.name();
    private static final String PETITIONER_TEMPLATE = EmailTemplateNames.PETITIONER_NOTICE_OF_PROCEEDINGS.name();

    private Map<String, Object> incomingPayload;
    private TaskContext taskContext;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SendNoticeOfProceedingsEmailTask sendNoticeOfProceedingsEmailTask;

    @Before
    public void setUp() {
        incomingPayload = new HashMap<>();
        incomingPayload.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        incomingPayload.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        incomingPayload.put(D_8_CASE_REFERENCE, TEST_D8_CASE_REFERENCE);

        incomingPayload.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        incomingPayload.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);

        incomingPayload.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);

        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void shouldSendNotificationEmailToSolicitor_whenPetitionerIsRepresented() throws TaskException {
        incomingPayload.put(D_8_PETITIONER_EMAIL, null);
        incomingPayload.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        taskContext.setTransientObject(CASE_EVENT_ID_JSON_KEY, ISSUE_AOS);

        executeTask();

        verify(emailService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLICITOR_TEMPLATE),
            argThat(new HamcrestArgumentMatcher<>(
                    allOf(
                        hasEntry(NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID),
                        hasEntry(NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                        hasEntry(NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME),
                        hasEntry(NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME)
                    )
                )
            ),
            anyString(),
            eq(LanguagePreference.ENGLISH)
        );
    }

    @Test
    public void shouldSendNotificationEmailToPetitioner_whenPetitionerIsNotRepresented() throws TaskException {
        incomingPayload.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        incomingPayload.put(PETITIONER_SOLICITOR_EMAIL, null);
        taskContext.setTransientObject(CASE_EVENT_ID_JSON_KEY, EVENT_ISSUE_AOS);

        executeTask();

        verify(emailService).sendEmail(
            eq(TEST_PETITIONER_EMAIL),
            eq(PETITIONER_TEMPLATE),
            argThat(new HamcrestArgumentMatcher<>(
                    allOf(
                        hasEntry(NOTIFICATION_CASE_NUMBER_KEY, TEST_D8_CASE_REFERENCE),
                        hasEntry(NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME),
                        hasEntry(NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME)
                    )
                )
            ),
            anyString(),
            eq(LanguagePreference.ENGLISH)
        );
    }

    @Test
    public void isEventSupportedShouldReturnTrue() {
        assertThat(SendNoticeOfProceedingsEmailTask.isEventSupported(EVENT_ISSUE_AOS_FROM_REISSUE), is(true));
        assertThat(SendNoticeOfProceedingsEmailTask.isEventSupported(EVENT_ISSUE_AOS), is(true));
    }

    @Test
    public void isEventSupportedShouldReturnFalse() {
        assertThat(SendNoticeOfProceedingsEmailTask.isEventSupported("any other"), is(false));
        assertThat(SendNoticeOfProceedingsEmailTask.isEventSupported("submit"), is(false));
        assertThat(SendNoticeOfProceedingsEmailTask.isEventSupported(""), is(false));
        assertThat(SendNoticeOfProceedingsEmailTask.isEventSupported(null), is(false));
    }

    private void executeTask() throws TaskException {
        Map<String, Object> returnedPayload = sendNoticeOfProceedingsEmailTask.execute(taskContext, incomingPayload);

        assertThat(returnedPayload, equalTo(incomingPayload));
    }
}
