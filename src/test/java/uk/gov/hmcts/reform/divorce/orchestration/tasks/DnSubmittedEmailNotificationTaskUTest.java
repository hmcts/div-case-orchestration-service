package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMAIL_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;

@RunWith(MockitoJUnitRunner.class)
public class DnSubmittedEmailNotificationTaskUTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DnSubmittedEmailNotificationTask target;

    @Test
    public void whenExecuteEmailNotificationTask_thenSendDNEmail() throws NotificationClientException {
        Map<String, Object> payload = ImmutableMap.of(
                D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME,
                D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME,
                D_8_PETITIONER_EMAIL, TEST_USER_EMAIL,
                D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID
        );
        TaskContext context = new DefaultTaskContext();
        Map<String, String> notificationTemplateVars = ImmutableMap.of(
            NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME,
            NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME,
            NOTIFICATION_REFERENCE_KEY, TEST_CASE_FAMILY_MAN_ID
        );
        target.execute(context, payload);

        verify(emailService).sendEmail(EmailTemplateNames.DN_SUBMISSION, TEST_USER_EMAIL, notificationTemplateVars);

    }

    @Test
    public void givenNotificationError_whenExecuteEmailNotificationTask_thenReturnEmailError() throws NotificationClientException {

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> payload = mock(Map.class);

        doThrow(new NotificationClientException(new Exception(TEST_ERROR)))
                .when(emailService).sendEmail(any(), any(), any());

        Map<String, Object> taskResponse = target.execute(context, payload);

        assertTrue(taskResponse.isEmpty());
        assertNotNull(context.getTransientObject(EMAIL_ERROR_KEY));
    }

    @Test
    public void givenNullInput_whenExecuteEmailNotificationTask_thenSendWithNullParameters() throws NotificationClientException {
        Map<String, Object> payload = new HashMap<>();
        payload.put(CASE_ID_JSON_KEY, null);
        payload.put(D_8_PETITIONER_FIRST_NAME, null);
        payload.put(D_8_PETITIONER_LAST_NAME, null);
        payload.put(D_8_PETITIONER_EMAIL, null);

        Map<String, String> notificationTemplateVars = new HashMap<>();
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, null);
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, null);
        notificationTemplateVars.put(NOTIFICATION_REFERENCE_KEY, null);

        TaskContext context = new DefaultTaskContext();

        target.execute(context, payload);

        verify(emailService).sendEmail(EmailTemplateNames.DN_SUBMISSION, null, notificationTemplateVars);

    }

}
