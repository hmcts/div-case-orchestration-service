package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class RespondentSolicitorSendEmailTaskTest {

    public static final EmailTemplateNames EMAIL_TEMPLATE_ID = EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY;

    @Mock
    private EmailService emailService;

    private final RespondentSolicitorSendEmailTask task = getRespondentSolicitorSendEmailTaskInstance();

    @Test
    public void getPersonalisation() {
        Map<String, Object> caseData = ImmutableMap.of(
            PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME,
            PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME,
            RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME,
            RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME,
            RESPONDENT_SOLICITOR_NAME, TEST_RESPONDENT_SOLICITOR_NAME
        );

        assertThat(task.getPersonalisation(context(), caseData), is(
            ImmutableMap.of(
                NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME,
                NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME,
                NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID,
                NOTIFICATION_SOLICITOR_NAME, TEST_RESPONDENT_SOLICITOR_NAME
            )
        ));
    }

    @Test
    public void getRecipientEmail() {
        Map<String, Object> caseData = ImmutableMap.of(
            RESPONDENT_SOLICITOR_EMAIL, TEST_RESPONDENT_SOLICITOR_EMAIL
        );

        assertThat(task.getRecipientEmail(caseData), is(TEST_RESPONDENT_SOLICITOR_EMAIL));
    }

    private RespondentSolicitorSendEmailTask getRespondentSolicitorSendEmailTaskInstance() {
        return new RespondentSolicitorSendEmailTask(emailService) {
            @Override
            protected EmailTemplateNames getTemplate() {
                return EMAIL_TEMPLATE_ID;
            }
        };
    }
}