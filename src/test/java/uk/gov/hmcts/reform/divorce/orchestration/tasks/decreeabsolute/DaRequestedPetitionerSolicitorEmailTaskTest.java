package uk.gov.hmcts.reform.divorce.orchestration.tasks.decreeabsolute;

import com.google.common.collect.ImmutableMap;
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

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.DA_APPLICATION_HAS_BEEN_RECEIVED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class DaRequestedPetitionerSolicitorEmailTaskTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DaRequestedPetitionerSolicitorEmailTask task;

    @Test
    public void shouldSendEmailWhenExecuteEmailNotificationTaskIsCalled() throws TaskException {
        Map<String, Object> caseData = buildCaseData();

        executeTask(caseData);

        verifyEmailSent(context(), caseData);
    }

    @Test
    public void shouldReturnTemplate() {
        EmailTemplateNames returnedTemplate = task.getTemplate();

        assertThat(DA_APPLICATION_HAS_BEEN_RECEIVED, is(returnedTemplate));
    }

    @Test
    public void canEmailBeSentShouldReturnTrueWhenPetitionerSolicitorEmailIsPopulated() {
        Map<String, Object> caseData = buildCaseData();

        assertThat(task.canEmailBeSent(caseData), is(true));
    }

    @Test
    public void canEmailBeSentShouldReturnTrueWhenPetitionerSolicitorEmailIsNotProvided() {
        Map<String, Object> caseData = buildCaseData();
        caseData.remove(PETITIONER_SOLICITOR_EMAIL);

        assertThat(task.canEmailBeSent(caseData), is(false));
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithRespondent();

        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);


        return caseData;
    }

    private void executeTask(Map<String, Object> caseData) {
        Map<String, Object> returnPayload = task.execute(context(), caseData);
        assertThat(caseData, is(returnPayload));
    }

    private void verifyEmailSent(TaskContext context, Map<String, Object> caseData) {
        verify(emailService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            DA_APPLICATION_HAS_BEEN_RECEIVED.name(),
            getExpectedTemplateVars(caseData),
            "Clark Kent vs Diana Prince: Decree absolute application submitted",
            LanguagePreference.ENGLISH
        );
    }

    private Map<String, String> getExpectedTemplateVars(Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID,
            NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(caseData)
        );
    }
}
