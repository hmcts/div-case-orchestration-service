package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;

@RunWith(MockitoJUnitRunner.class)
public class SendPetitionerNotificationEmailTest {

    private static final String TEST_COURT_KEY = "westMidlands";
    private static final String TEST_COURT_DISPLAY_NAME = "West Midlands Regional Divorce Centre";
    private static final String SERVICE_CENTRE_KEY = "serviceCentre";
    private static final String SERVICE_CENTRE_DISPLAY_NAME = "Courts and Tribunals Service Centre";

    private static final String D8_CASE_ID = "LV17D80101";
    private static final String UNFORMATTED_CASE_ID = "0123456789";

    @Mock
    EmailService emailService;

    @Mock
    private TaskCommons taskCommons;

    @InjectMocks
    SendPetitionerSubmissionNotificationEmail sendPetitionerSubmissionNotificationEmail;

    @InjectMocks
    SendPetitionerGenericUpdateNotificationEmail sendPetitionerGenericUpdateNotificationEmail;

    private Map<String, Object> testData;
    private TaskContext context;
    private Map<String, String> expectedTemplateVars;

    @Before
    public void setup() throws TaskException {
        testData = new HashMap<>();
        testData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        testData.put(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
        testData.put(D_8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(DIVORCE_UNIT_JSON_KEY, TEST_COURT_KEY);

        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);

        expectedTemplateVars = new HashMap<>();

        expectedTemplateVars.put("email address", TEST_USER_EMAIL);
        expectedTemplateVars.put("first name", TEST_PETITIONER_FIRST_NAME);
        expectedTemplateVars.put("last name", TEST_PETITIONER_LAST_NAME);
        expectedTemplateVars.put("CCD reference", D8_CASE_ID);

        mockTestCourtsLookup();
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
    public void shouldCallEmailServiceForGenericUpdate() {
        when(emailService.sendPetitionerGenericUpdateNotificationEmail(TEST_USER_EMAIL, expectedTemplateVars))
                .thenReturn(null);

        assertEquals(testData, sendPetitionerGenericUpdateNotificationEmail.execute(context, testData));

        verify(emailService).sendPetitionerGenericUpdateNotificationEmail(TEST_USER_EMAIL, expectedTemplateVars);
    }

    @Test
    public void shouldCallEmailServiceWithNoCaseIdFormatWhenNoUnableToFormatIdForGenericUpdate() {
        expectedTemplateVars.replace("CCD reference", D8_CASE_ID);

        when(emailService.sendPetitionerGenericUpdateNotificationEmail(TEST_USER_EMAIL, expectedTemplateVars))
                .thenReturn(null);

        assertEquals(testData, sendPetitionerGenericUpdateNotificationEmail.execute(context, testData));

        verify(emailService).sendPetitionerGenericUpdateNotificationEmail(TEST_USER_EMAIL, expectedTemplateVars);
    }

    @Test
    public void shouldCallEmailService_WithCourtName_WhenCaseIsAssignedToCourt() throws TaskException {
        expectedTemplateVars.put("RDC name", TEST_COURT_DISPLAY_NAME);
        expectedTemplateVars.replace("CCD reference", UNFORMATTED_CASE_ID);

        when(emailService.sendPetitionerSubmissionNotificationEmail(TEST_USER_EMAIL, expectedTemplateVars))
                .thenReturn(null);

        assertEquals(testData, sendPetitionerSubmissionNotificationEmail.execute(context, testData));

        verify(emailService).sendPetitionerSubmissionNotificationEmail(TEST_USER_EMAIL, expectedTemplateVars);
    }

    @Test
    public void shouldCallEmailService_WithServiceCentreName_WhenCaseIsAssignedToServiceCentre() throws TaskException {
        testData.put(DIVORCE_UNIT_JSON_KEY, SERVICE_CENTRE_KEY);
        expectedTemplateVars.replace("CCD reference", UNFORMATTED_CASE_ID);
        expectedTemplateVars.put("RDC name", SERVICE_CENTRE_DISPLAY_NAME);

        when(emailService.sendPetitionerSubmissionNotificationEmail(TEST_USER_EMAIL, expectedTemplateVars))
                .thenReturn(null);

        assertEquals(testData, sendPetitionerSubmissionNotificationEmail.execute(context, testData));

        verify(emailService).sendPetitionerSubmissionNotificationEmail(TEST_USER_EMAIL, expectedTemplateVars);
    }

    @Test
    public void shouldCallEmailServiceWithNoCaseIdFormatWhenNoUnableToFormatIdForSubmission() throws TaskException {
        expectedTemplateVars.replace("CCD reference", UNFORMATTED_CASE_ID);
        expectedTemplateVars.put("RDC name", TEST_COURT_DISPLAY_NAME);

        when(emailService.sendPetitionerSubmissionNotificationEmail(TEST_USER_EMAIL, expectedTemplateVars))
                .thenReturn(null);

        assertEquals(testData, sendPetitionerSubmissionNotificationEmail.execute(context, testData));

        verify(emailService).sendPetitionerSubmissionNotificationEmail(TEST_USER_EMAIL, expectedTemplateVars);
    }

}