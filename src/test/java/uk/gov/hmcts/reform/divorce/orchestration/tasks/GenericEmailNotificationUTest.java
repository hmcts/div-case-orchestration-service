package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.EmailService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMAIL_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;

@RunWith(MockitoJUnitRunner.class)
public class GenericEmailNotificationUTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private GenericEmailNotification genericEmailNotification;

    @Test
    public void whenExecuteEmailTask_thenEmailServiceIsCalled() throws Exception {
        TaskContext context = new DefaultTaskContext();

        Map<String, String>  vars = mock(Map.class);
        context.setTransientObject(NOTIFICATION_EMAIL, TEST_USER_EMAIL);
        context.setTransientObject(NOTIFICATION_TEMPLATE, EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT);
        context.setTransientObject(NOTIFICATION_TEMPLATE_VARS, vars);

        Map<String, Object>  data = mock(Map.class);
        Map<String, Object> taskResponse = genericEmailNotification.execute(context, data);

        verify(emailService, times(1))
                .sendEmail(EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT, TEST_USER_EMAIL, vars);
        assertEquals(taskResponse, data);
    }

    @Test
    public void givenError_whenExecuteEmailTask_thenReturnErrorInTaskContext() throws Exception {
        TaskContext context = new DefaultTaskContext();

        Map<String, String>  vars = mock(Map.class);
        context.setTransientObject(NOTIFICATION_EMAIL, TEST_USER_EMAIL);
        context.setTransientObject(NOTIFICATION_TEMPLATE, EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT);
        context.setTransientObject(NOTIFICATION_TEMPLATE_VARS, vars);

        Map<String, Object>  data = mock(Map.class);
        Exception clientException = new Exception("Error");
        doThrow(new NotificationClientException(clientException)).when(emailService)
                .sendEmail(EmailTemplateNames.RESPONDENT_SUBMISSION_CONSENT, TEST_USER_EMAIL, vars);

        Map<String, Object> taskResponse = genericEmailNotification.execute(context, data);

        assertTrue(taskResponse.isEmpty());
        assertNotNull(context.getTransientObject(EMAIL_ERROR_KEY));
    }
}
