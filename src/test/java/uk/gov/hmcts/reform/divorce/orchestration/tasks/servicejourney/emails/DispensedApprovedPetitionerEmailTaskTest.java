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
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.AddresseeDataExtractorTest;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.CITIZEN_DISPENSED_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.EmailDataExtractable.CaseDataKeys.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceJourneyEmailTaskHelper.getExpectedNotificationTemplateVars;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceJourneyEmailTaskHelper.removeAllEmailAddresses;

@RunWith(MockitoJUnitRunner.class)
public class DispensedApprovedPetitionerEmailTaskTest {

    public static final String EXPECTED_SUBJECT = "Your ‘dispense with service’ application has been approved";
    @Mock
    private EmailService emailService;

    @InjectMocks
    private DispensedApprovedPetitionerEmailTask task;

    private DefaultTaskContext testContext;
    private static EmailTemplateNames TEST_TEMPLATE = CITIZEN_DISPENSED_APPROVED;

    @Before
    public void setUp() {
        testContext = new DefaultTaskContext();
        testContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void shouldSendEmail_ToCitizen_whenExecuteEmailNotificationTask() throws TaskException {
        Map<String, Object> caseData = buildCaseData();
        caseData.remove(PETITIONER_SOLICITOR_EMAIL);

        executeTask(caseData);

        verifyCitizenEmailSent(caseData);
    }

    @Test
    public void shouldNotSendEmail_whenEmptyRecipientEmail() {
        Map<String, Object> caseData = buildCaseData();

        removeAllEmailAddresses(caseData);

        executeTask(caseData);

        verifyNoInteractions(emailService);
    }

    @Test
    public void shouldReturnTemplate() {
        EmailTemplateNames returnedTemplate = task.getTemplate();

        assertEquals(TEST_TEMPLATE, returnedTemplate);
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithRespondent();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        caseData.put(PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);

        return caseData;
    }

    private void executeTask(Map<String, Object> caseData) {
        Map<String, Object> returnPayload = task.execute(getTaskContext(), caseData);
        assertEquals(caseData, returnPayload);
    }

    private void verifyCitizenEmailSent(Map<String, Object> caseData) {
        verify(emailService).sendEmail(
            TEST_PETITIONER_EMAIL,
            TEST_TEMPLATE.name(),
            getExpectedNotificationTemplateVars(false, testContext, caseData),
            EXPECTED_SUBJECT,
            LanguagePreference.ENGLISH
        );
    }

    public static TaskContext getTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        return context;
    }
}
