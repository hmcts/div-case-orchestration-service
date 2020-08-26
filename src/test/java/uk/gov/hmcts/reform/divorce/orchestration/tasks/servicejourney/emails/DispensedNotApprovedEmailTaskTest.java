package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.CITIZEN_DISPENSED_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_DISPENSED_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedEmailTaskTest.getTaskContext;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedNotApprovedEmailTask.citizenSubject;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedNotApprovedEmailTask.solicitorSubject;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceJourneyEmailTaskHelper.executeTask;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceJourneyEmailTaskHelper.getCitizenTemplateVariables;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceJourneyEmailTaskHelper.getSolicitorTemplateVariables;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceJourneyEmailTaskHelper.removeAllEmailAddresses;

@RunWith(MockitoJUnitRunner.class)
public class DispensedNotApprovedEmailTaskTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DispensedNotApprovedEmailTask dispensedNotApprovedEmailTask;

    private Map<String, Object> caseData;
    private DefaultTaskContext testContext;

    @Before
    public void setUp() {
        testContext = new DefaultTaskContext();
        testContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void shouldSendEmail_ToCitizen_whenExecuteEmailNotificationTask() throws TaskException {
        caseData = buildCaseData(false);
        caseData.remove(PETITIONER_SOLICITOR_EMAIL);

        executeTask(dispensedNotApprovedEmailTask, caseData);

        verifyCitizenEmailSent(caseData);
    }

    @Test
    public void shouldSendEmail_ToSolicitor_whenExecuteEmailNotificationTask() throws TaskException {
        caseData = buildCaseData(true);

        executeTask(dispensedNotApprovedEmailTask, caseData);

        verifySolicitorEmailSent(caseData);
    }

    @Test
    public void shouldNotSendEmail_whenEmptyRecipientEmail() {
        caseData = buildCaseData(false);

        removeAllEmailAddresses(caseData);

        executeTask(dispensedNotApprovedEmailTask, caseData);

        verifyZeroInteractions(emailService);
    }

    @Test
    public void shouldReturnPersonalisationFor_Citizen() {
        caseData = buildCaseData(false);

        executePersonalisation(false, caseData);
    }

    @Test
    public void shouldReturnPersonalisationFor_Solicitor() {
        caseData = buildCaseData(true);

        executePersonalisation(true, caseData);
    }

    @Test
    public void shouldReturnTemplateFor_Solicitor() {
        caseData = buildCaseData(true);
        dispensedNotApprovedEmailTask.getTemplate(caseData);

        assertEquals(SOL_DISPENSED_NOT_APPROVED, dispensedNotApprovedEmailTask.getTemplate(caseData));
    }

    @Test
    public void shouldReturnTemplateFor_Citizen() {
        caseData = buildCaseData(false);
        dispensedNotApprovedEmailTask.getTemplate(caseData);

        assertEquals(CITIZEN_DISPENSED_NOT_APPROVED, dispensedNotApprovedEmailTask.getTemplate(caseData));
    }

    @Test
    public void shouldReturnSubjectFor_Solicitor() {
        caseData = buildCaseData(true);
        dispensedNotApprovedEmailTask.getSubject(caseData);

        assertEquals(dispensedNotApprovedEmailTask.getSubject(caseData), solicitorSubject);
    }

    @Test
    public void shouldReturnSubjectFor_Citizen() {
        caseData = buildCaseData(false);
        dispensedNotApprovedEmailTask.getSubject(caseData);

        assertEquals(dispensedNotApprovedEmailTask.getSubject(caseData), citizenSubject);
    }

    private Map<String, Object> buildCaseData(boolean isPetitionerRepresented) {
        caseData = isPetitionerRepresented
            ? AddresseeDataExtractorTest.buildCaseDataWithPetitionerSolicitor()
            : AddresseeDataExtractorTest.buildCaseDataWithRespondent();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        caseData.put(PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);

        return caseData;
    }

    private void executePersonalisation(boolean isPetitionerRepresented, Map<String, Object> caseData) {
        Map returnPayload = dispensedNotApprovedEmailTask.getPersonalisation(getTaskContext(), caseData);

        Map expectedPayload = getExpectedNotificationTemplateVars(isPetitionerRepresented, testContext, caseData);

        assertEquals(returnPayload, expectedPayload);
    }

    private void verifySolicitorEmailSent(Map<String, Object> caseData) {
        verify(emailService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            SOL_DISPENSED_NOT_APPROVED.name(),
            getExpectedNotificationTemplateVars(true, testContext, caseData),
            dispensedNotApprovedEmailTask.getSubject(caseData),
            LanguagePreference.ENGLISH
        );
    }

    private void verifyCitizenEmailSent(Map<String, Object> caseData) {
        verify(emailService).sendEmail(
            TEST_PETITIONER_EMAIL,
            CITIZEN_DISPENSED_NOT_APPROVED.name(),
            getExpectedNotificationTemplateVars(false, testContext, caseData),
            dispensedNotApprovedEmailTask.getSubject(caseData),
            LanguagePreference.ENGLISH
        );
    }

    private static Map<String, String> getExpectedNotificationTemplateVars(
        boolean isPetitionerRepresented, TaskContext taskContext, Map<String, Object> caseData) {
        if (isPetitionerRepresented) {
            return getSolicitorTemplateVariables(taskContext, caseData);
        } else {
            return getCitizenTemplateVariables(caseData);
        }
    }
}
