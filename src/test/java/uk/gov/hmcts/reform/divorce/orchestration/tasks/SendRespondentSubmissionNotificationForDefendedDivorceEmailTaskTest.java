package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DIVORCE_UNIT_WEST_MIDLANDS;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INFERRED_MALE_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SendRespondentSubmissionNotificationForDefendedDivorceEmailTaskTest {

    @Mock
    private CcdUtil ccdUtil;

    @Mock
    private TaskCommons taskCommons;

    @Mock
    private TemplateConfigService templateConfigService;

    @Captor
    private ArgumentCaptor<EmailTemplateNames> emailTemplateNamesCaptor;

    @InjectMocks
    private SendRespondentSubmissionNotificationForDefendedDivorceEmailTask classUnderTest;

    private TaskContext context;
    private Court testCourt;
    private static final String FORM_SUBMISSION_DUE_DATE = "20 September 2018";
    private static final String FORM_WELSH_SUBMISSION_DUE_DATE = "20 Medi 2018";

    @Before()
    public void setUp() {
        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        when(ccdUtil.getFormattedDueDate(any(), any())).thenReturn(FORM_SUBMISSION_DUE_DATE);
        when(ccdUtil.getWelshFormattedDate(isA(Map.class), anyString())).thenReturn(FORM_WELSH_SUBMISSION_DUE_DATE);

        testCourt = new Court();
        testCourt.setDivorceCentreName("West Midlands Regional Divorce Centre");
        testCourt.setPoBox("PO Box 3650");
        testCourt.setCourtCity("Stoke-on-Trent");
        testCourt.setPostCode("ST4 9NH");

        when(taskCommons.getCourt("westMidlands")).thenReturn(testCourt);
    }

    @Test
    public void shouldExecuteTaskWithValidDefendedDivorceEmailTemplate() {
        Map<String, Object> caseData = new HashMap<>(buildCaseData());

        classUnderTest.execute(context, caseData);

        verify(taskCommons).sendEmail(
            emailTemplateNamesCaptor.capture(),
            anyString(),
            anyString(),
            anyMap(),
            any(LanguagePreference.class)
        );

        EmailTemplateNames actualEmailTemplate = emailTemplateNamesCaptor.getValue();
        assertThat(classUnderTest.getEmailTemplateName(), is(actualEmailTemplate));
        verify(templateConfigService, times(2)).getRelationshipTermByGender(
            anyString(), any(LanguagePreference.class));
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D_8_CASE_REFERENCE, TEST_CASE_ID);
        caseData.put(D_8_INFERRED_PETITIONER_GENDER, TEST_INFERRED_MALE_GENDER);
        caseData.put(DIVORCE_UNIT_JSON_KEY, TEST_DIVORCE_UNIT_WEST_MIDLANDS);
        caseData.put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL);
        caseData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        return caseData;
    }
}