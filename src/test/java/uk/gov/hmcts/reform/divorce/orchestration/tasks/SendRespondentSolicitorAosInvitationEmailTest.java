package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SendRespondentSolicitorAosInvitationEmailTest {

    @Mock
    private TaskCommons taskCommons;

    @InjectMocks
    SendRespondentSolicitorAosInvitationEmail sendRespondentSolicitorAosInvitationEmail;

    @Test
    public void testExecuteSendsEmailToRespondentSolicitorWithAccessCode() throws TaskException {
        // given
        String respondentSolicitorEmail = "solicitor@localhost.local";

        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(D8_RESPONDENT_SOLICITOR_EMAIL, respondentSolicitorEmail);
        caseData.put(D8_RESPONDENT_SOLICITOR_NAME, "sol name");
        caseData.put(RESP_FIRST_NAME_CCD_FIELD, "resp first");
        caseData.put(RESP_LAST_NAME_CCD_FIELD, "resp last");
        caseData.put(D_8_CASE_REFERENCE, "LV17D80100");

        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, "1111222233334444");
        context.setTransientObject(RESPONDENT_PIN, "A1B2C3D4");

        Map<String, String> expectedTemplateVars = new HashMap<>();
        expectedTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, "LV17D80100");
        expectedTemplateVars.put("solicitors name", "sol name");
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, "resp first");
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, "resp last");
        expectedTemplateVars.put("access code", "A1B2C3D4");
        expectedTemplateVars.put(NOTIFICATION_CASE_NUMBER_KEY, "1111-2222-3333-4444");

        // when
        Map<String, Object> returnedPayload = sendRespondentSolicitorAosInvitationEmail.execute(context, caseData);

        //then
        assertThat(caseData, is(sameInstance(returnedPayload)));
        verify(taskCommons).sendEmail(
                EmailTemplateNames.RESPONDENT_SOLICITOR_AOS_INVITATION,
                "Respondent solicitor's AOS invitation",
                respondentSolicitorEmail,
                expectedTemplateVars
        );
        assertThat(context.hasTaskFailed(), is(false));
    }

    @Test
    public void testDefaultsSolicitorNameIfNoneIsProvided() throws TaskException {
        // given
        String respondentSolicitorEmail = "solicitor@localhost.local";

        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(D8_RESPONDENT_SOLICITOR_EMAIL, respondentSolicitorEmail);
        caseData.put(RESP_FIRST_NAME_CCD_FIELD, "resp first");
        caseData.put(RESP_LAST_NAME_CCD_FIELD, "resp last");
        caseData.put(D_8_CASE_REFERENCE, "LV17D80100");

        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, "1111222233334444");
        context.setTransientObject(RESPONDENT_PIN, "A1B2C3D4");

        Map<String, String> expectedTemplateVars = new HashMap<>();
        expectedTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, "LV17D80100");
        expectedTemplateVars.put("solicitors name", "Sir/Madam");
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, "resp first");
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, "resp last");
        expectedTemplateVars.put("access code", "A1B2C3D4");
        expectedTemplateVars.put(NOTIFICATION_CASE_NUMBER_KEY, "1111-2222-3333-4444");

        // when
        Map<String, Object> returnedPayload = sendRespondentSolicitorAosInvitationEmail.execute(context, caseData);

        //then
        assertThat(caseData, is(sameInstance(returnedPayload)));
        verify(taskCommons).sendEmail(
                EmailTemplateNames.RESPONDENT_SOLICITOR_AOS_INVITATION,
                "Respondent solicitor's AOS invitation",
                respondentSolicitorEmail,
                expectedTemplateVars
        );
        assertThat(context.hasTaskFailed(), is(false));
    }

    @Test(expected = TaskException.class)
    public void testExecuteSendsEmailToRespondentSolicitorWithAccessCodeFailsTaskIfExceptionIsThrown() throws TaskException {
        // given
        HashMap<String, Object> caseData = new HashMap<>();
        DefaultTaskContext context = new DefaultTaskContext();
        doThrow(new TaskException("Something went wrong")).when(taskCommons).sendEmail(any(), any(), any(), any());

        // when
        Map<String, Object> returnedPayload = sendRespondentSolicitorAosInvitationEmail.execute(context, caseData);

        //then
        assertThat(caseData, is(sameInstance(returnedPayload)));
        assertThat(context.hasTaskFailed(), is(true));
    }
}
