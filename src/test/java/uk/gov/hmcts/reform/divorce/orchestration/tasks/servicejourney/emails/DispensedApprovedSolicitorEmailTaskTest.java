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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_DISPENSED_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceJourneyEmailTaskHelper.getExpectedNotificationTemplateVars;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceJourneyEmailTaskHelper.getTaskContext;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceJourneyEmailTaskHelper.removeAllEmailAddresses;

@RunWith(MockitoJUnitRunner.class)
public class DispensedApprovedSolicitorEmailTaskTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DispensedApprovedSolicitorEmailTask task;

    private Map<String, Object> caseData;
    private DefaultTaskContext testContext;
    private static EmailTemplateNames TEST_TEMPLATE = SOL_DISPENSED_APPROVED;

    @Before
    public void setUp() {
        testContext = new DefaultTaskContext();
        testContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void shouldSendEmail_ToSolicitor_whenExecuteEmailNotificationTask() throws TaskException {
        caseData = buildCaseData();

        executeTask(caseData);

        verifySolicitorEmailSent(caseData);
    }

    @Test
    public void shouldNotSendEmail_whenEmptyRecipientEmail() {
        caseData = buildCaseData();

        removeAllEmailAddresses(caseData);

        executeTask(caseData);

        verifyZeroInteractions(emailService);
    }

    @Test
    public void shouldReturnPersonalisationFor_Solicitor() {
        caseData = buildCaseData();

        executePersonalisation(caseData);
    }

    @Test
    public void shouldReturnTemplateFor_Solicitor() {
        caseData = buildCaseData();

        EmailTemplateNames returnedTemplate = task.getTemplate();

        assertEquals(TEST_TEMPLATE, returnedTemplate);
    }

    @Test
    public void shouldReturnSubjectFor_Solicitor() {
        caseData = buildCaseData();

        String returnedSubject = task.getSubject(caseData);

        assertEquals(returnedSubject, task.SUBJECT);
    }

    private Map<String, Object> buildCaseData() {
        caseData = AddresseeDataExtractorTest.buildCaseDataWithPetitionerSolicitor();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        caseData.put(PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);

        return caseData;
    }

    private void executeTask(Map<String, Object> caseData) {
        Map returnPayload = task.execute(getTaskContext(), caseData);
        assertEquals(caseData, returnPayload);
    }

    private void executePersonalisation(Map<String, Object> caseData) {
        Map returnPersonalisation = task.getPersonalisation(getTaskContext(), caseData);

        Map expectedPersonalisation = getExpectedNotificationTemplateVars(true, testContext, caseData);

        assertEquals(returnPersonalisation, expectedPersonalisation);
    }

    private void verifySolicitorEmailSent(Map<String, Object> caseData) {
        verify(emailService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            TEST_TEMPLATE.name(),
            getExpectedNotificationTemplateVars(true, testContext, caseData),
            task.getSubject(caseData),
            LanguagePreference.ENGLISH
        );
    }
}
