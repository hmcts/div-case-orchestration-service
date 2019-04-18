package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SEND_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class EmailNotificationTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailNotification target;

    @Test
    public void givenSendEmailTrue_whenExecuteEmailNotificationTask_thenSendSaveDraftConfirmationEmailIsCalled() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(NOTIFICATION_SEND_EMAIL, Boolean.TRUE.toString());
        context.setTransientObject(NOTIFICATION_EMAIL, TEST_USER_EMAIL);
        Map<String, Object> payload = mock(Map.class);

        target.execute(context, payload);

        verify(emailService).sendEmail(TEST_USER_EMAIL, EmailTemplateNames.SAVE_DRAFT.name(), null, "draft saved confirmation");
    }

    @Test
    public void givenSendEmailIsString_whenExecuteEmailNotificationTask_thenSendSaveDraftConfirmationEmailIsCalled() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(NOTIFICATION_SEND_EMAIL, TEST_USER_EMAIL);
        context.setTransientObject(NOTIFICATION_EMAIL, TEST_USER_EMAIL);
        Map<String, Object> payload = mock(Map.class);

        target.execute(context, payload);

        verify(emailService).sendEmail(TEST_USER_EMAIL, EmailTemplateNames.SAVE_DRAFT.name(), null, "draft saved confirmation");
    }

    @Test
    public void givenSendEmailFalse_whenExecuteEmailNotificationTask_thenSendSaveDraftConfirmationEmailNotCalled() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(NOTIFICATION_SEND_EMAIL, Boolean.FALSE.toString());
        context.setTransientObject(NOTIFICATION_EMAIL, TEST_USER_EMAIL);
        Map<String, Object> payload = mock(Map.class);

        target.execute(context, payload);

        verify(emailService, never()).sendEmail(TEST_USER_EMAIL, EmailTemplateNames.SAVE_DRAFT.name(), null, "draft saved confirmation");
    }

    @Test
    public void givenSendEmailNull_whenExecuteEmailNotificationTask_thenSendSaveDraftConfirmationEmailNotCalled() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(NOTIFICATION_SEND_EMAIL, null);
        context.setTransientObject(NOTIFICATION_EMAIL, TEST_USER_EMAIL);
        Map<String, Object> payload = mock(Map.class);

        target.execute(context, payload);

        verify(emailService, never()).sendEmail(TEST_USER_EMAIL, EmailTemplateNames.SAVE_DRAFT.name(), null, "draft saved confirmation");
    }

    @Test
    public void givenBlankEmail_whenExecuteEmailNotificationTask_thenSendSaveDraftConfirmationEmailNotCalled() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(NOTIFICATION_SEND_EMAIL, Boolean.TRUE.toString());
        Map<String, Object> payload = mock(Map.class);

        target.execute(context, payload);

        verify(emailService, never()).sendEmail(TEST_USER_EMAIL, EmailTemplateNames.SAVE_DRAFT.name(), null, "draft saved confirmation");
    }
}
