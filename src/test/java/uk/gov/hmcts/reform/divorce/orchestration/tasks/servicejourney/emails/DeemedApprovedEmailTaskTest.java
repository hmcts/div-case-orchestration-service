package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class DeemedApprovedEmailTaskTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DeemedApprovedEmailTask deemedApprovedEmailTask;

    @Test
    public void whenExecuteEmailNotificationTask_thenSendEmail() {
        Map<String, Object> caseData = buildCaseData();
        TaskContext context = getTaskContext();
        Map<String, String> notificationTemplateVars = getExpectedNotificationTemplateVars();

        deemedApprovedEmailTask.execute(context, caseData);

        verify(emailService).sendEmail(
            TEST_USER_EMAIL,
            EmailTemplateNames.CITIZEN_DEEMED_APPROVED.name(),
            notificationTemplateVars,
            deemedApprovedEmailTask.getSubject(),
            LanguagePreference.ENGLISH
        );
    }

    private Map<String, String> getExpectedNotificationTemplateVars() {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME
        );
    }

    private TaskContext getTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        return context;
    }

    private ImmutableMap<String, Object> buildCaseData() {
        return ImmutableMap.of(
            D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME,
            D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME,
            D_8_PETITIONER_EMAIL, TEST_USER_EMAIL,
            LANGUAGE_PREFERENCE_WELSH, NO_VALUE
        );
    }
}
