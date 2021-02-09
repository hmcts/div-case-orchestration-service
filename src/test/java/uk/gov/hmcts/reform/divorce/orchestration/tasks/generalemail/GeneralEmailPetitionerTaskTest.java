package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.GENERAL_EMAIL_PETITIONER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.CaseDataKeys.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.getExpectedNotificationTemplateVars;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.getNotRepresentedSubject;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class GeneralEmailPetitionerTaskTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private GeneralEmailPetitionerTask task;

    @Test
    public void shouldSendEmailWhenExecuteEmailNotificationTaskIsCalled() throws TaskException {
        Map<String, Object> caseData = buildCaseData();

        executeTask(caseData);

        verifyEmailSent(context(), caseData);
    }

    @Test
    public void shouldReturnTemplate() {
        EmailTemplateNames returnedTemplate = task.getTemplate();

        assertThat(GENERAL_EMAIL_PETITIONER, is(returnedTemplate));
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithRespondent();
        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);

        caseData.put(PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        caseData.put(GENERAL_EMAIL_DETAILS, TEST_GENERAL_EMAIL_DETAILS);

        return caseData;
    }

    private void executeTask(Map<String, Object> caseData) {
        Map<String, Object> returnPayload = task.execute(context(), caseData);
        assertThat(caseData, is(returnPayload));
    }

    private void verifyEmailSent(TaskContext context, Map<String, Object> caseData) {
        verify(emailService).sendEmail(
            TEST_PETITIONER_EMAIL,
            GENERAL_EMAIL_PETITIONER.name(),
            getExpectedNotificationTemplateVars(GeneralEmailTaskHelper.Party.PETITIONER, context, caseData),
            getNotRepresentedSubject(context),
            LanguagePreference.ENGLISH
        );
    }
}