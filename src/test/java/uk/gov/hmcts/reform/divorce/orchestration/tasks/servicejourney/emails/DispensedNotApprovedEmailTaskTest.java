package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
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
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceJourneyEmailTaskHelper.getExpectedNotificationTemplateVars;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceJourneyEmailTaskHelper.removeAllEmailAddresses;

@RunWith(MockitoJUnitRunner.class)
public class DispensedNotApprovedEmailTaskTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DispensedNotApprovedEmailTask task;

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

        task.execute(getTaskContext(), caseData);

        verifyCitizenEmailSent(caseData);
    }

    @Test
    public void shouldSendEmail_ToSolicitor_whenExecuteEmailNotificationTask() throws TaskException {
        caseData = buildCaseData(true);

        task.execute(getTaskContext(), caseData);

        verifySolicitorEmailSent(caseData);
    }

    @Test
    public void shouldNotSendEmail_whenEmptyRecipientEmail() {
        caseData = buildCaseData(false);

        removeAllEmailAddresses(caseData);

        task.execute(getTaskContext(), caseData);

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

        EmailTemplateNames returnedTemplate = task.getTemplate(caseData);

        assertEquals(SOL_DISPENSED_NOT_APPROVED, returnedTemplate);
    }

    @Test
    public void shouldReturnTemplateFor_Citizen() {
        caseData = buildCaseData(false);

        EmailTemplateNames returnedTemplate = task.getTemplate(caseData);

        assertEquals(CITIZEN_DISPENSED_NOT_APPROVED, returnedTemplate);
    }

    @Test
    public void shouldReturnSubjectFor_Solicitor() {
        caseData = buildCaseData(true);

        String returnedSubject = task.getSubject(caseData);

        assertEquals(returnedSubject, solicitorSubject);
    }

    @Test
    public void shouldReturnSubjectFor_Citizen() {
        caseData = buildCaseData(false);

        String returnedSubject = task.getSubject(caseData);

        assertEquals(returnedSubject, citizenSubject);
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
        Map returnPayload = task.getPersonalisation(getTaskContext(), caseData);

        Map expectedPayload = getExpectedNotificationTemplateVars(isPetitionerRepresented, testContext, caseData);

        assertEquals(returnPayload, expectedPayload);
    }

    private void verifySolicitorEmailSent(Map<String, Object> caseData) {
        verify(emailService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            SOL_DISPENSED_NOT_APPROVED.name(),
            getExpectedNotificationTemplateVars(true, testContext, caseData),
            task.getSubject(caseData),
            LanguagePreference.ENGLISH
        );
    }

    private void verifyCitizenEmailSent(Map<String, Object> caseData) {
        verify(emailService).sendEmail(
            TEST_PETITIONER_EMAIL,
            CITIZEN_DISPENSED_NOT_APPROVED.name(),
            getExpectedNotificationTemplateVars(false, testContext, caseData),
            task.getSubject(caseData),
            LanguagePreference.ENGLISH
        );
    }
}
