package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;

@RunWith(MockitoJUnitRunner.class)
public class SendSubmissionNotificationEmailTest {

    public static final String COURT_DISPLAY_NAME = "eastMidlands";
    public static final String UNFORMATTED_CASE_ID = "0123456789012345";
    public static final String FORMATTED_CASE_ID = "0123-4567-8901-2345";

    @Mock
    EmailService emailService;

    @InjectMocks
    SendSubmissionNotificationEmail sendSubmissionNotificationEmail;

    private Map<String, Object> testData;
    private TaskContext context;
    private Map<String, String> templateVars;

    @Before
    public void setup() {
        testData = new HashMap<>();
        testData.put(D_8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        testData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        testData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        testData.put(DIVORCE_UNIT_JSON_KEY, COURT_DISPLAY_NAME);

        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);

        templateVars = new HashMap<>();

        templateVars.put("email address", TEST_USER_EMAIL);
        templateVars.put("first name", TEST_PETITIONER_FIRST_NAME);
        templateVars.put("last name", TEST_PETITIONER_LAST_NAME);
        templateVars.put("RDC name", CourtEnum.valueOf(
            COURT_DISPLAY_NAME.toUpperCase(Locale.UK)).getDisplayName()
        );
        templateVars.put("CCD reference", FORMATTED_CASE_ID);
    }

    @Test
    public void executeShouldSetDateAndCourtDetailsOnPayload() {
        when(emailService.sendSubmissionNotificationEmail(TEST_USER_EMAIL, templateVars)).thenReturn(null);

        assertEquals(testData, sendSubmissionNotificationEmail.execute(context, testData));

        verify(emailService).sendSubmissionNotificationEmail(TEST_USER_EMAIL, templateVars);
    }
}
