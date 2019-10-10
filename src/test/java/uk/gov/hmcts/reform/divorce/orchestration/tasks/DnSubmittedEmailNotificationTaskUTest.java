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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class DnSubmittedEmailNotificationTaskUTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DnSubmittedEmailNotificationTask target;

    @Test
    public void whenExecuteEmailNotificationTask_NoSolicitor_thenSendPetDNEmail() throws NotificationClientException {
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

        verify(emailService).sendEmailAndReturnExceptionIfFails(TEST_USER_EMAIL,
            EmailTemplateNames.DN_SUBMISSION.name(),
            notificationTemplateVars,
            "DN Submission");

    }

    @Test
    public void whenExecuteEmailNotificationTask_SolicitorExists_thenSendSolDNEmail() throws NotificationClientException {
        Map<String, Object> payload = new HashMap<>();
        payload.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        payload.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        payload.put(PET_SOL_EMAIL, TEST_USER_EMAIL);
        payload.put(D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);
        payload.put(RESP_FIRST_NAME_CCD_FIELD, TEST_USER_FIRST_NAME);
        payload.put(RESP_LAST_NAME_CCD_FIELD, TEST_USER_LAST_NAME);
        payload.put(PET_SOL_NAME, TEST_SOLICITOR_NAME);

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        Map<String, String> notificationTemplateVars = ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID,
            NOTIFICATION_EMAIL, TEST_USER_EMAIL,
            NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME,
            NOTIFICATION_RESP_NAME, TEST_USER_FIRST_NAME + " " + TEST_USER_LAST_NAME,
            NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME
        );
        target.execute(context, payload);

        verify(emailService).sendEmailAndReturnExceptionIfFails(TEST_USER_EMAIL,
            EmailTemplateNames.SOL_APPLICANT_DN_SUBMITTED.name(),
            notificationTemplateVars,
            "DN Submission");

    }

    @Test
    public void givenNotificationError_whenExecuteEmailNotificationTask_thenReturnData() throws NotificationClientException {

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> payload = mock(Map.class);

        doThrow(new NotificationClientException(new Exception(TEST_ERROR)))
                .when(emailService).sendEmailAndReturnExceptionIfFails(any(), any(), any(), any());

        Map<String, Object> taskResponse = target.execute(context, payload);

        assertEquals(taskResponse, payload);
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

        verify(emailService).sendEmailAndReturnExceptionIfFails(null,
            EmailTemplateNames.DN_SUBMISSION.name(),
            notificationTemplateVars,
            "DN Submission");

    }

}
