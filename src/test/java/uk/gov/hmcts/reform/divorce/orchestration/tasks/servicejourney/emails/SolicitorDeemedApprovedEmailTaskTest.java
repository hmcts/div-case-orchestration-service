package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedEmailTaskTest.getTaskContext;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorDeemedApprovedEmailTaskTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SolicitorDeemedApprovedEmailTask solicitorDeemedApprovedEmailTask;

    @Test
    public void whenExecuteEmailNotificationTask_thenSendEmail() {
        Map<String, Object> caseData = buildCaseData();

        solicitorDeemedApprovedEmailTask.execute(getTaskContext(), caseData);

        verify(emailService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            EmailTemplateNames.PET_SOL_DEEMED_APPROVED.name(),
            getExpectedNotificationTemplateVars(),
            solicitorDeemedApprovedEmailTask.getSubject(caseData),
            LanguagePreference.ENGLISH
        );
    }

    @Test
    public void getSubjectRendersValidSubject() {
        Map<String, Object> caseData = buildCaseData();

        assertThat(
            solicitorDeemedApprovedEmailTask.getSubject(caseData),
            is("Clark Kent vs Diana Prince: Deemed service application has been approved")
        );
    }

    public static Map<String, String> getExpectedNotificationTemplateVars() {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME,
            NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME,
            NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID,
            NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME
        );
    }

    public static Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        caseData.put(D_8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        caseData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(LANGUAGE_PREFERENCE_WELSH, NO_VALUE);

        return caseData;
    }
}
