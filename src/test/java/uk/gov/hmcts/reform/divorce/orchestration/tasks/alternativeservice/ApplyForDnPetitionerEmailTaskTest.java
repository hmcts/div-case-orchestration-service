package uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP_HUSBAND;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Gender.MALE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.CITIZEN_APPLY_FOR_DN_ALTERNATIVE_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.CaseDataKeys.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceJourneyEmailTaskHelper.getTaskContext;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceJourneyEmailTaskHelper.removeAllEmailAddresses;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class ApplyForDnPetitionerEmailTaskTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ApplyForDnPetitionerEmailTask task;

    @Test
    public void shouldSendEmailWhenExecuteEmailNotificationTaskIsCalled() throws TaskException {
        Map<String, Object> caseData = buildCaseData();
        caseData.remove(PETITIONER_SOLICITOR_EMAIL);

        executeTask(caseData);

        verifyCitizenEmailSent(caseData);
    }

    @Test
    public void shouldNotSendEmailWhenEmptyRecipientEmail() {
        Map<String, Object> caseData = buildCaseData();

        removeAllEmailAddresses(caseData);

        executeTask(caseData);

        verifyNoInteractions(emailService);
    }

    @Test
    public void shouldReturnTemplate() {
        EmailTemplateNames returnedTemplate = task.getTemplate();

        assertEquals(CITIZEN_APPLY_FOR_DN_ALTERNATIVE_SERVICE, returnedTemplate);
    }

    @Test
    public void shouldReturnDataForTemplate() {
        Map<String, Object> caseData = buildCaseData();

        Map<String, String> templateVars = task.getPersonalisation(context(), caseData);

        assertEquals(templateVars, getExpectedTemplateVars(caseData));
    }

    private Map<String, String> getExpectedTemplateVars(Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME,
            NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_FAMILY_MAN_ID,
            NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP_HUSBAND
        );
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithRespondent();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        caseData.put(D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_INFERRED_PETITIONER_GENDER, MALE.getValue());

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
            CITIZEN_APPLY_FOR_DN_ALTERNATIVE_SERVICE.name(),
            getExpectedTemplateVars(caseData),
            "Your can now apply for you DN",
            LanguagePreference.ENGLISH
        );
    }
}
