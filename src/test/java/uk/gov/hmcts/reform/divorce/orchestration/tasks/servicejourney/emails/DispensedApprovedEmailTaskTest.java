package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedEmailTaskTest.buildCaseData;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedEmailTaskTest.getExpectedNotificationTemplateVars;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedEmailTaskTest.getTaskContext;

@RunWith(MockitoJUnitRunner.class)
public class DispensedApprovedEmailTaskTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DispensedApprovedEmailTask dispensedApprovedEmailTask;

    @Test
    public void whenExecuteEmailNotificationTask_thenSendEmail() {
        Map<String, Object> caseData = buildCaseData();
        TaskContext context = getTaskContext();
        Map<String, String> notificationTemplateVars = getExpectedNotificationTemplateVars();

        dispensedApprovedEmailTask.execute(context, caseData);

        verify(emailService).sendEmail(
            TEST_USER_EMAIL,
            EmailTemplateNames.CITIZEN_DISPENSED_APPROVED.name(),
            notificationTemplateVars,
            dispensedApprovedEmailTask.getSubject(),
            LanguagePreference.ENGLISH
        );
    }
}
