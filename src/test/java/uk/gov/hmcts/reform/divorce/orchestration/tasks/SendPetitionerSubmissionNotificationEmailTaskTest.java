package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.D8_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_REASON_UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.UNFORMATTED_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCED_WHO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RDC_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(SpringRunner.class)
public class SendPetitionerSubmissionNotificationEmailTaskTest {

    private static final String TEST_COURT_KEY = "westMidlands";
    private static final String TEST_COURT_DISPLAY_NAME = "West Midlands Regional Divorce Centre";
    private static final String SERVICE_CENTRE_KEY = "serviceCentre";
    private static final String SERVICE_CENTRE_DISPLAY_NAME = "Courts and Tribunals Service Centre";
    private static final String CASE_REFERENCE_KEY = "CaseReference";

    @Mock
    EmailService emailService;

    @Mock
    private TaskCommons taskCommons;

    @InjectMocks
    SendPetitionerSubmissionNotificationEmailTask sendPetitionerSubmissionNotificationEmailTask;

    private Map<String, Object> testData;
    private TaskContext context;
    private Map<String, String> expectedTemplateVars;

    @Before
    public void setup() throws TaskException {
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);

        testData = new HashMap<>();
        expectedTemplateVars = new HashMap<>();

        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME);
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME);
        expectedTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, D8_CASE_ID);

        mockTestCourtsLookup();
    }

    private void addPetitionerTestData() {
        testData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        testData.put(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
        testData.put(D_8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(NOTIFICATION_RELATIONSHIP_KEY, TEST_RELATIONSHIP);
        testData.put(D_8_REASON_FOR_DIVORCE, TEST_REASON_UNREASONABLE_BEHAVIOUR);
        testData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        testData.put(DIVORCE_UNIT_JSON_KEY, TEST_COURT_KEY);
        testData.put(D_8_DIVORCED_WHO, TEST_RELATIONSHIP);
    }

    private void addSolicitorTestData() {
        testData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        testData.put(D_8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        testData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        testData.put(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        testData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        testData.put(DIVORCE_UNIT_JSON_KEY, TEST_COURT_KEY);
        testData.put(PREVIOUS_CASE_ID_CCD_KEY, Collections.singletonMap(CASE_REFERENCE_KEY, TEST_CASE_ID));
    }

    private void mockTestCourtsLookup() throws TaskException {
        Court testCourt = new Court();
        testCourt.setDivorceCentreName(TEST_COURT_DISPLAY_NAME);
        when(taskCommons.getCourt(TEST_COURT_KEY)).thenReturn(testCourt);

        Court testCourtServiceCentre = new Court();
        testCourtServiceCentre.setServiceCentreName(SERVICE_CENTRE_DISPLAY_NAME);
        when(taskCommons.getCourt(SERVICE_CENTRE_KEY)).thenReturn(testCourtServiceCentre);
    }

    @Test
    public void shouldCallEmailService_WithCourtName_WhenCaseIsAssignedToCourt() throws TaskException {
        addPetitionerTestData();

        expectedTemplateVars.put(NOTIFICATION_RDC_NAME_KEY, TEST_COURT_DISPLAY_NAME);
        expectedTemplateVars.replace(NOTIFICATION_CCD_REFERENCE_KEY, UNFORMATTED_CASE_ID);

        Map returnPayload = sendPetitionerSubmissionNotificationEmailTask.execute(context, testData);

        assertEquals(testData, returnPayload);

        verify(emailService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(EmailTemplateNames.APPLIC_SUBMISSION.name()),
            eq(expectedTemplateVars),
            any(),
            eq(LanguagePreference.ENGLISH));
    }

    @Test
    public void shouldCallEmailService_WithServiceCentreName_WhenCaseIsAssignedToServiceCentre() throws TaskException {
        addPetitionerTestData();
        testData.put(DIVORCE_UNIT_JSON_KEY, SERVICE_CENTRE_KEY);
        testData.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);

        expectedTemplateVars.replace(NOTIFICATION_CCD_REFERENCE_KEY, UNFORMATTED_CASE_ID);
        expectedTemplateVars.put(NOTIFICATION_RDC_NAME_KEY, SERVICE_CENTRE_DISPLAY_NAME);

        Map returnPayload = sendPetitionerSubmissionNotificationEmailTask.execute(context, testData);

        assertEquals(testData, returnPayload);

        verify(emailService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(EmailTemplateNames.APPLIC_SUBMISSION.name()),
            eq(expectedTemplateVars),
            any(),
            eq(LanguagePreference.WELSH));
    }

    @Test
    public void shouldCallEmailServiceWithNoCaseIdFormatWhenNoUnableToFormatIdForSubmission() throws TaskException {
        addPetitionerTestData();
        testData.put(LANGUAGE_PREFERENCE_WELSH, null);
        expectedTemplateVars.replace(NOTIFICATION_CCD_REFERENCE_KEY, UNFORMATTED_CASE_ID);
        expectedTemplateVars.put(NOTIFICATION_RDC_NAME_KEY, TEST_COURT_DISPLAY_NAME);

        Map returnPayload = sendPetitionerSubmissionNotificationEmailTask.execute(context, testData);

        assertEquals(testData, returnPayload);

        verify(emailService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(EmailTemplateNames.APPLIC_SUBMISSION.name()),
            eq(expectedTemplateVars),
            any(),
            eq(LanguagePreference.ENGLISH));
    }

    @Test
    public void shouldCallEmailService_WithAmendTemplate_WhenPreviousCaseIdExists() throws TaskException {
        addPetitionerTestData();
        testData.put(DIVORCE_UNIT_JSON_KEY, SERVICE_CENTRE_KEY);
        testData.put(PREVIOUS_CASE_ID_CCD_KEY, Collections.singletonMap(CASE_REFERENCE_KEY, TEST_CASE_ID));
        testData.put(LANGUAGE_PREFERENCE_WELSH, NO_VALUE);
        expectedTemplateVars.replace(NOTIFICATION_CCD_REFERENCE_KEY, UNFORMATTED_CASE_ID);
        expectedTemplateVars.put(NOTIFICATION_RDC_NAME_KEY, SERVICE_CENTRE_DISPLAY_NAME);

        Map returnPayload = sendPetitionerSubmissionNotificationEmailTask.execute(context, testData);

        assertEquals(testData, returnPayload);

        verify(emailService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(EmailTemplateNames.APPLIC_SUBMISSION_AMEND.name()),
            eq(expectedTemplateVars),
            any(),
            eq(LanguagePreference.ENGLISH));
    }

    @Test
    public void shouldCallEmailService_WithAmendSolicitorTemplate_WhenPreviousCaseIdExists() throws TaskException {
        addSolicitorTestData();
        setupSolicitorDocumentData();

        Map returnPayload = sendPetitionerSubmissionNotificationEmailTask.execute(context, testData);

        assertEquals(testData, returnPayload);

        verify(emailService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(EmailTemplateNames.APPLIC_SUBMISSION_AMEND_SOLICITOR.name()),
            eq(expectedTemplateVars),
            any(),
            eq(LanguagePreference.ENGLISH));
    }

    @Test
    public void shouldNotCallEmailService_whenNewCaseAndPetitionerEmailDoesNotExist() throws TaskException {
        addPetitionerTestData();
        testData.remove(D_8_PETITIONER_EMAIL);
        testData.remove(PREVIOUS_CASE_ID_CCD_KEY);

        Map<String, Object> returnedPayload = sendPetitionerSubmissionNotificationEmailTask.execute(context, testData);

        assertEquals(testData, returnedPayload);

        verifyZeroInteractions(emailService);
    }

    @Test
    public void shouldNotCallEmailService_whenNewCaseAndPetitionerEmailIsNull() throws TaskException {
        addPetitionerTestData();
        testData.put(D_8_PETITIONER_EMAIL, null);
        testData.remove(PREVIOUS_CASE_ID_CCD_KEY);

        Map<String, Object> returnedPayload = sendPetitionerSubmissionNotificationEmailTask.execute(context, testData);

        assertEquals(testData, returnedPayload);

        verifyZeroInteractions(emailService);
    }

    @Test
    public void shouldNotCallEmailService_whenNewCaseAndNoPetitionerEmailIsEmpty() throws TaskException {
        addPetitionerTestData();
        testData.put(D_8_PETITIONER_EMAIL, "");
        testData.remove(PREVIOUS_CASE_ID_CCD_KEY);

        Map<String, Object> returnedPayload = sendPetitionerSubmissionNotificationEmailTask.execute(context, testData);

        assertEquals(testData, returnedPayload);

        verifyZeroInteractions(emailService);
    }

    private void setupSolicitorDocumentData() {
        expectedTemplateVars.remove(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY);
        expectedTemplateVars.remove(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY);
        expectedTemplateVars.replace(NOTIFICATION_CCD_REFERENCE_KEY, UNFORMATTED_CASE_ID);
        expectedTemplateVars.put(NOTIFICATION_RDC_NAME_KEY, TEST_COURT_DISPLAY_NAME);
        expectedTemplateVars.put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME);
        expectedTemplateVars.put(NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME);
        expectedTemplateVars.put(NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
    }
}
