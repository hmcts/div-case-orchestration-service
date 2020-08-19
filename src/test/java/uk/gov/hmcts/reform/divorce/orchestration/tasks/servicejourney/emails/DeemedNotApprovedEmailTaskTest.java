package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedEmailTaskTest.buildCaseData;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedEmailTaskTest.getExpectedNotificationTemplateVars;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedEmailTaskTest.getTaskContext;

@RunWith(MockitoJUnitRunner.class)
public class DeemedNotApprovedEmailTaskTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DeemedNotApprovedEmailTask deemedNotApprovedEmailTask;

    @Test
    public void whenExecuteEmailNotificationTask_thenSendEmail() {
        Map<String, Object> caseData = buildCaseData();

        deemedNotApprovedEmailTask.execute(getTaskContext(), caseData);

        verify(emailService).sendEmail(
            TEST_USER_EMAIL,
            EmailTemplateNames.CITIZEN_DEEMED_NOT_APPROVED.name(),
            getExpectedNotificationTemplateVars(),
            deemedNotApprovedEmailTask.getSubject(caseData),
            LanguagePreference.ENGLISH
        );
    }

    @Test
    public void whenEmptyRecipientEmail_thenDoNotSendEmail() {
        Map<String, Object> caseData = buildCaseData();
        caseData.remove(PETITIONER_EMAIL);

        deemedNotApprovedEmailTask.execute(getTaskContext(), caseData);

        verifyZeroInteractions(emailService);
    }
}
