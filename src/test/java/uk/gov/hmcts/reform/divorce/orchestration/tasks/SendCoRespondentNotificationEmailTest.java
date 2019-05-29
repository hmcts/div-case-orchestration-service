package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;

@RunWith(SpringRunner.class)
public class SendCoRespondentNotificationEmailTest {

    private static final String D8_CASE_ID = "LV17D80101";
    private static final String UNFORMATTED_CASE_ID = "0123456789";

    private TaskContext context;
    private Map<String, Object> testData;
    private Map<String, String> expectedTemplateVars;

    @Mock
    EmailService emailService;

    @InjectMocks
    SendCoRespondentGenericUpdateNotificationEmail sendCoRespondentGenericUpdateNotificationEmail;


    @Before
    public void setup() {
        testData = new HashMap<>();
        testData.put(D_8_CASE_REFERENCE, D8_CASE_ID);
        testData.put(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
        testData.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, TEST_USER_FIRST_NAME);
        testData.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, TEST_USER_LAST_NAME);

        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);

        expectedTemplateVars = new HashMap<>();

        expectedTemplateVars.put("CCD reference", D8_CASE_ID);
        expectedTemplateVars.put("email address", TEST_USER_EMAIL);
        expectedTemplateVars.put("first name", TEST_USER_FIRST_NAME);
        expectedTemplateVars.put("last name", TEST_USER_LAST_NAME);
    }

    @Test
    public void shouldCallEmailServiceForGenericUpdateIfCoRespEmailExists() throws TaskException {

        testData.put(CO_RESP_EMAIL_ADDRESS, TEST_USER_EMAIL);

        Map returnPayload = sendCoRespondentGenericUpdateNotificationEmail.execute(context, testData);

        assertEquals(testData, returnPayload);

        verify(emailService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(EmailTemplateNames.GENERIC_UPDATE.name()),
            eq(expectedTemplateVars),
            any());
    }


    @Test
    public void shouldNotCallEmailServiceForCoRespGenericUpdateIfCoRespEmailDoesNotExist() throws TaskException {
        // make sure it isn't triggered

        Map returnPayload = sendCoRespondentGenericUpdateNotificationEmail.execute(context, testData);

        assertEquals(testData, returnPayload);

        verifyZeroInteractions(emailService);
    }
}
