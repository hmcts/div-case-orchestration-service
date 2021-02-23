package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_D8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.VALUE_NOT_SET;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class SendEmailTaskTest {

    public static final EmailTemplateNames EMAIL_TEMPLATE_ID = EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY;

    @Mock
    private EmailService emailService;

    private final SendEmailTask task = getSendEmailTaskInstance();

    @Test
    public void getSubjectShouldReturnCaseIdFromContext() {
        assertThat(
            task.getSubject(context(), EMPTY_MAP),
            is("CaseId: " + TEST_CASE_ID + ", email template " + EMAIL_TEMPLATE_ID.name())
        );
    }

    @Test
    public void getSubjectShouldReturnCaseReferenceFromCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D_8_CASE_REFERENCE, TEST_D8_CASE_REFERENCE);

        assertThat(
            task.getSubject(new DefaultTaskContext(), caseData),
            is("CaseId: " + TEST_D8_CASE_REFERENCE + ", email template " + EMAIL_TEMPLATE_ID.name())
        );
    }

    @Test
    public void getSubjectShouldReturnNoIdentifierWhenNoneGiven() {
        assertThat(
            task.getSubject(new DefaultTaskContext(), EMPTY_MAP),
            is("CaseId: " + VALUE_NOT_SET + ", email template " + EMAIL_TEMPLATE_ID.name())
        );
    }

    private SendEmailTask getSendEmailTaskInstance() {
        return new SendEmailTask(emailService) {
            @Override
            protected Map<String, String> getPersonalisation(TaskContext context, Map<String, Object> caseData) {
                return null;
            }

            @Override
            protected EmailTemplateNames getTemplate() {
                return EMAIL_TEMPLATE_ID;
            }

            @Override
            protected String getRecipientEmail(Map<String, Object> caseData) {
                return TEST_EMAIL;
            }
        };
    }

    @Test
    public void getLanguageShouldReturnEnglishWhenNotSpecified() {
        assertThat(task.getLanguage(emptyMap()), is(LanguagePreference.ENGLISH));
    }

    @Test
    public void getLanguageShouldReturnEnglishWhenSetEnglish() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(LANGUAGE_PREFERENCE_WELSH, NO_VALUE);

        assertThat(task.getLanguage(emptyMap()), is(LanguagePreference.ENGLISH));
    }

    @Test
    public void getLanguageShouldReturnWelshWhenSetWelsh() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);

        assertThat(task.getLanguage(caseData), is(LanguagePreference.WELSH));
    }

    @Test
    public void canEmailBeSentShouldReturnTrue() {
        assertThat(task.canEmailBeSent(emptyMap()), is(true));
    }
}