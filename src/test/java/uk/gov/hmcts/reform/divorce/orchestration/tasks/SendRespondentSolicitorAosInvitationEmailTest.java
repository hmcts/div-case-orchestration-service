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
        caseData.put("D8RespondentSolicitorEmail", respondentSolicitorEmail);
        caseData.put("D8RespondentSolicitorName", "sol name");
        caseData.put("D8RespondentFirstName", "resp first");
        caseData.put("D8RespondentLastName", "resp last");

        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject("caseId", "1111222233334444");
        context.setTransientObject("pin", "A1B2C3D4");

        Map<String, String> expectedTemplateVars = new HashMap<>();
        expectedTemplateVars.put("CCD reference", "1111-2222-3333-4444");
        expectedTemplateVars.put("solicitors name", "sol name");
        expectedTemplateVars.put("first name", "resp first");
        expectedTemplateVars.put("last name", "resp last");
        expectedTemplateVars.put("access code", "A1B2C3D4");

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
