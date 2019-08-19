package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.D8_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.UNFORMATTED_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SendSolicitorPersonalServiceEmailTaskTest {

    private static final String SOLICITOR_PERSONAL_SERVICE_EMAIL = "Solicitor Personal Service email";
    private Map<String, Object> testData;
    private TaskContext context;
    private Map<String, String> expectedTemplateVars;

    @Mock
    EmailService emailService;

    @InjectMocks
    SendSolicitorPersonalServiceEmailTask sendSolicitorPersonalServiceEmailTask;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);

        testData = new HashMap<>();
        testData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);

        expectedTemplateVars = new HashMap<>();

        expectedTemplateVars.put(NOTIFICATION_EMAIL, TEST_USER_EMAIL);
    }

    @Test
    public void testExecuteCallsEmailServiceWithTheCorrectParams() throws NotificationClientException, TaskException {
        //given
        testData.put(PET_SOL_EMAIL, TEST_USER_EMAIL);
        testData.put(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
        testData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_USER_FIRST_NAME);
        testData.put(RESP_LAST_NAME_CCD_FIELD, TEST_USER_LAST_NAME);
        testData.put(PET_SOL_NAME, TEST_SOLICITOR_NAME);

        expectedTemplateVars.put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME);
        expectedTemplateVars.put(NOTIFICATION_RESP_NAME, TEST_USER_FIRST_NAME + " " + TEST_USER_LAST_NAME);
        expectedTemplateVars.put(NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        expectedTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, UNFORMATTED_CASE_ID);

        //when
        sendSolicitorPersonalServiceEmailTask.execute(context, testData);

        //then
        verify(emailService).sendEmailAndReturnExceptionIfFails(
                eq(TEST_USER_EMAIL),
                eq(EmailTemplateNames.SOL_PERSONAL_SERVICE.name()),
                eq(expectedTemplateVars),
                eq(SOLICITOR_PERSONAL_SERVICE_EMAIL)
        );
    }
}
