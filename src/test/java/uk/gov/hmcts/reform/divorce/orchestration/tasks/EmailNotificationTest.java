package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class EmailNotificationTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailNotification target;

    @Test
    public void whenExecuteEmailNotificationTask_thenSendSaveDraftConfirmationEmailIsCalled() {
        TaskContext context = mock(TaskContext.class);
        Map<String, Object> payload = mock(Map.class);

        target.execute(context, payload, AUTH_TOKEN, TEST_USER_EMAIL);

        verify(emailService).sendSaveDraftConfirmationEmail(TEST_USER_EMAIL);
    }

    @Test
    public void givenBlankEmail_whenExecuteEmailNotificationTask_thenSendSaveDraftConfirmationEmailNoyCalled() {
        TaskContext context = mock(TaskContext.class);
        Map<String, Object> payload = mock(Map.class);

        target.execute(context, payload, AUTH_TOKEN, null);

        verify(emailService, never()).sendSaveDraftConfirmationEmail(TEST_USER_EMAIL);
    }

}
