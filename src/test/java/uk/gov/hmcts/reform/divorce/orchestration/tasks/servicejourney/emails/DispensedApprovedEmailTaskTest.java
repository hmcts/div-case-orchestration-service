package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import com.google.common.collect.ImmutableMap;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.CITIZEN_DISPENSED_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_DISPENSED_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedEmailTaskTest.getTaskContext;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedApprovedEmailTask.CITIZEN_SUBJECT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedApprovedEmailTask.SOLICITOR_SUBJECT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@RunWith(MockitoJUnitRunner.class)
public class DispensedApprovedEmailTaskTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DispensedApprovedEmailTask dispensedApprovedEmailTask;

    private Map<String, Object> caseData;
    private DefaultTaskContext testContext;

    @Before
    public void setUp() {
        testContext = new DefaultTaskContext();
        testContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void whenExecuteEmailNotificationTask_thenSendEmail_ToCitizen() throws TaskException {
        caseData = buildCaseData(false);
        caseData.remove(PETITIONER_SOLICITOR_EMAIL);

        executeTask(caseData);

        verifyCitizenEmailSent(caseData);
    }

    @Test
    public void whenExecuteEmailNotificationTask_thenSendEmail_ToSolicitor() throws TaskException {
        caseData = buildCaseData(true);

        executeTask(caseData);

        verifySolicitorEmailSent(caseData);
    }

    @Test
    public void whenEmptyRecipientEmail_thenDoNotSendEmail() {
        caseData = buildCaseData(false);

        removeAllEmailAddresses(caseData);

        executeTask(caseData);

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
        dispensedApprovedEmailTask.getTemplate(caseData);

        assertEquals(dispensedApprovedEmailTask.getTemplate(caseData), SOL_DISPENSED_APPROVED);
    }

    @Test
    public void shouldReturnTemplateFor_Citizen() {
        caseData = buildCaseData(false);
        dispensedApprovedEmailTask.getTemplate(caseData);

        assertEquals(dispensedApprovedEmailTask.getTemplate(caseData), CITIZEN_DISPENSED_APPROVED);
    }

    @Test
    public void shouldReturnSubjectFor_Solicitor() {
        caseData = buildCaseData(true);
        dispensedApprovedEmailTask.getSubject(caseData);

        assertEquals(dispensedApprovedEmailTask.getSubject(caseData), SOLICITOR_SUBJECT);
    }

    @Test
    public void shouldReturnSubjectFor_Citizen() {
        caseData = buildCaseData(false);
        dispensedApprovedEmailTask.getSubject(caseData);

        assertEquals(dispensedApprovedEmailTask.getSubject(caseData), CITIZEN_SUBJECT);
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

    private void executeTask(Map<String, Object> caseData) {
        Map returnPayload = dispensedApprovedEmailTask.execute(getTaskContext(), caseData);
        assertEquals(caseData, returnPayload);
    }

    private void executePersonalisation(boolean isPetitionerRepresented, Map<String, Object> caseData) {
        Map returnPayload = dispensedApprovedEmailTask.getPersonalisation(getTaskContext(), caseData);

        Map expectedPayload = getExpectedNotificationTemplateVars(isPetitionerRepresented, testContext, caseData);

        assertEquals(returnPayload, expectedPayload);
    }

    private void verifySolicitorEmailSent(Map<String, Object> caseData) {
        verify(emailService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            SOL_DISPENSED_APPROVED.name(),
            getExpectedNotificationTemplateVars(true, testContext, caseData),
            dispensedApprovedEmailTask.getSubject(caseData),
            LanguagePreference.ENGLISH
        );
    }

    private void verifyCitizenEmailSent(Map<String, Object> caseData) {
        verify(emailService).sendEmail(
            TEST_PETITIONER_EMAIL,
            CITIZEN_DISPENSED_APPROVED.name(),
            getExpectedNotificationTemplateVars(false, testContext, caseData),
            dispensedApprovedEmailTask.getSubject(caseData),
            LanguagePreference.ENGLISH
        );
    }

    private void removeAllEmailAddresses(Map<String, Object> caseData) {
        caseData.remove(PETITIONER_EMAIL);
        caseData.remove(PETITIONER_SOLICITOR_EMAIL);
    }

    private static Map<String, String> getExpectedNotificationTemplateVars(
        boolean isPetitionerRepresented, TaskContext taskContext, Map<String, Object> caseData) {
        if (isPetitionerRepresented) {
            return ImmutableMap.of(
                NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
                NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
                NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
                NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(caseData)
            );
        } else {
            return ImmutableMap.of(
                NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME
            );
        }
    }
}

