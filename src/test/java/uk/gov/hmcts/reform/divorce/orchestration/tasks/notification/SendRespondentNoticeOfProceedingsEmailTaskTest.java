package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ORGANISATION_POLICY_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ORGANISATION_POLICY_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EmailConstants.RESPONDENT_SOLICITOR_ORGANISATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_SOLICITOR_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.SendPetitionerNoticeOfProceedingsEmailTask.EVENT_ISSUE_AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.SendPetitionerNoticeOfProceedingsEmailTask.EVENT_ISSUE_AOS_FROM_REISSUE;

@RunWith(MockitoJUnitRunner.class)
public class SendRespondentNoticeOfProceedingsEmailTaskTest {

    private static final String TEST_CASE_REFERENCE = "TEST_CASE_REFERENCE";
    private static final String SOLICITOR_EMAIL = "solicitor@divorce.gov.uk";
    private static final String SOLICITOR_NAME = "Saul Goodman";
    private static final String PETITIONER_FIRST_NAME = "Jerry";
    private static final String PETITIONER_LAST_NAME = "Johnson";
    private static final String RESPONDENT_FIRST_NAME = "Inalota";
    private static final String RESPONDENT_LAST_NAME = "Trooble";
    private static final String PETITIONER_NAME = PETITIONER_FIRST_NAME + " " + PETITIONER_LAST_NAME;
    private static final String RESPONDENT_NAME = RESPONDENT_FIRST_NAME + " " + RESPONDENT_LAST_NAME;
    private static final String SOLICITOR_TEMPLATE = EmailTemplateNames.SOL_RESPONDENT_NOTICE_OF_PROCEEDINGS.name();
    private static final OrganisationPolicy SOLICITOR_ORGANISATION = buildOrganisationPolicy();

    private Map<String, Object> incomingPayload;
    private TaskContext taskContext;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SendRespondentNoticeOfProceedingsEmailTask sendRespondentNoticeOfProceedingsEmailTask;

    @Before
    public void setUp() {
        incomingPayload = new HashMap<>();
        incomingPayload.put(D_8_PETITIONER_FIRST_NAME, PETITIONER_FIRST_NAME);
        incomingPayload.put(D_8_PETITIONER_LAST_NAME, PETITIONER_LAST_NAME);
        incomingPayload.put(D_8_CASE_REFERENCE, TEST_CASE_REFERENCE);

        incomingPayload.put(RESP_FIRST_NAME_CCD_FIELD, RESPONDENT_FIRST_NAME);
        incomingPayload.put(RESP_LAST_NAME_CCD_FIELD, RESPONDENT_LAST_NAME);

        incomingPayload.put(RESPONDENT_SOLICITOR_NAME, SOLICITOR_NAME);

        incomingPayload.put(RESPONDENT_SOLICITOR_ORGANISATION_POLICY, SOLICITOR_ORGANISATION);

        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void shouldSendNotificationEmailToRespondentSolicitor_whenRespondentSolicitorIsDigital() throws TaskException {
        incomingPayload.put(RESPONDENT_SOLICITOR_EMAIL_ADDRESS, SOLICITOR_EMAIL);
        taskContext.setTransientObject(CASE_EVENT_ID_JSON_KEY, EVENT_ISSUE_AOS);

        executeTask();

        verify(emailService).sendEmail(
            eq(SOLICITOR_EMAIL),
            eq(SOLICITOR_TEMPLATE),
            argThat(new HamcrestArgumentMatcher<>(
                    allOf(
                        hasEntry(NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID),
                        hasEntry(NOTIFICATION_SOLICITOR_NAME, SOLICITOR_NAME),
                        hasEntry(NOTIFICATION_PET_NAME, PETITIONER_NAME),
                        hasEntry(NOTIFICATION_RESP_NAME, RESPONDENT_NAME),
                        hasEntry(RESPONDENT_SOLICITOR_ORGANISATION, TEST_ORGANISATION_POLICY_NAME)
                    )
                )
            ),
            anyString(),
            eq(LanguagePreference.ENGLISH)
        );
    }

    @Test
    public void shouldSendNotificationEmailToRespondentSolicitor_whenRespondentSolicitorIsNotDigital() throws TaskException {
        incomingPayload.put(RESPONDENT_SOLICITOR_EMAIL_ADDRESS, SOLICITOR_EMAIL);
        incomingPayload.put(RESPONDENT_SOLICITOR_ORGANISATION_POLICY, null);
        taskContext.setTransientObject(CASE_EVENT_ID_JSON_KEY, EVENT_ISSUE_AOS);

        executeTask();

        verifyNoInteractions(emailService);
    }

    @Test
    public void isEventSupportedShouldReturnTrue() {
        assertThat(SendPetitionerNoticeOfProceedingsEmailTask.isEventSupported(EVENT_ISSUE_AOS_FROM_REISSUE), is(true));
        assertThat(SendPetitionerNoticeOfProceedingsEmailTask.isEventSupported(EVENT_ISSUE_AOS), is(true));
    }

    @Test
    public void isEventSupportedShouldReturnFalse() {
        assertThat(SendPetitionerNoticeOfProceedingsEmailTask.isEventSupported("any other"), is(false));
        assertThat(SendPetitionerNoticeOfProceedingsEmailTask.isEventSupported("submit"), is(false));
        assertThat(SendPetitionerNoticeOfProceedingsEmailTask.isEventSupported(""), is(false));
        assertThat(SendPetitionerNoticeOfProceedingsEmailTask.isEventSupported(null), is(false));
    }

    private static OrganisationPolicy buildOrganisationPolicy() {
        return OrganisationPolicy.builder()
            .organisation(
                Organisation.builder()
                    .organisationID(TEST_ORGANISATION_POLICY_ID)
                    .organisationName(TEST_ORGANISATION_POLICY_NAME)
                    .build())
            .build();
    }

    private void executeTask() throws TaskException {
        Map<String, Object> returnedPayload = sendRespondentNoticeOfProceedingsEmailTask.execute(taskContext, incomingPayload);

        assertThat(returnedPayload, equalTo(incomingPayload));
    }
}
