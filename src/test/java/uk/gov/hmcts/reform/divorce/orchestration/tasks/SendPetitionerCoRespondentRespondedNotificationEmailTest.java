package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(MockitoJUnitRunner.class)
public class SendPetitionerCoRespondentRespondedNotificationEmailTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SendPetitionerCoRespondentRespondedNotificationEmail sendPetitionerCoRespondentRespondedNotificationEmail;

    @SuppressWarnings("unchecked")
    @Test
    public void testRightEmailIsSent_WhenCoRespondentSubmitsAndRespondentHasNot()
            throws TaskException, IOException {
        CcdCallbackRequest incomingPayload = getJsonFromResourceFile(
                "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = spy(incomingPayload.getCaseDetails().getCaseData());

        Map<String, String> expectedTemplateVars = new HashMap<>();
        expectedTemplateVars.put(NOTIFICATION_REFERENCE_KEY, (String) caseData.get(D_8_CASE_REFERENCE));
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_FIRST_NAME));
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_LAST_NAME));

        String petitionerEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);
        when(emailService.sendEmail(petitionerEmail,
                EmailTemplateNames.APPLICANT_CO_RESPONDENT_RESPONDS_AOS_NOT_SUBMITTED.name(),
                expectedTemplateVars,
            "co-respondent responded but respondent has not")
        ).thenReturn(null);

        DefaultTaskContext context = new DefaultTaskContext();

        Map<String, Object> returnedPayload = sendPetitionerCoRespondentRespondedNotificationEmail.execute(context, caseData);

        assertThat(caseData, is(sameInstance(returnedPayload)));
        verify(emailService).sendEmail(petitionerEmail,
            EmailTemplateNames.APPLICANT_CO_RESPONDENT_RESPONDS_AOS_NOT_SUBMITTED.name(),
            expectedTemplateVars,
            "co-respondent responded but respondent has not");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRightEmailIsSent_WhenCoRespondentSubmitsAndRespondentHasNotDefended()
            throws TaskException, IOException {
        CcdCallbackRequest incomingPayload = getJsonFromResourceFile(
                "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = spy(incomingPayload.getCaseDetails().getCaseData());
        caseData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);

        Map<String, String> expectedTemplateVars = new HashMap<>();
        expectedTemplateVars.put(NOTIFICATION_REFERENCE_KEY, (String) caseData.get(D_8_CASE_REFERENCE));
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_FIRST_NAME));
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_LAST_NAME));

        String petitionerEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);
        when(emailService.sendEmail(petitionerEmail,
                EmailTemplateNames.APPLICANT_CO_RESPONDENT_RESPONDS_AOS_SUBMITTED_NO_DEFEND.name(),
                expectedTemplateVars,
            "co-respondent responded when aos is undefended")
        ).thenReturn(null);

        DefaultTaskContext context = new DefaultTaskContext();

        Map<String, Object> returnedPayload = sendPetitionerCoRespondentRespondedNotificationEmail.execute(context, caseData);

        assertThat(caseData, is(sameInstance(returnedPayload)));
        verify(emailService).sendEmail(petitionerEmail,
            EmailTemplateNames.APPLICANT_CO_RESPONDENT_RESPONDS_AOS_SUBMITTED_NO_DEFEND.name(),
            expectedTemplateVars,
            "co-respondent responded when aos is undefended");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRightEmailIsSent_WhenCoRespondentSubmitsAndRespondentHasIsDefending()
            throws TaskException, IOException {
        CcdCallbackRequest incomingPayload = getJsonFromResourceFile(
                "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = spy(incomingPayload.getCaseDetails().getCaseData());
        caseData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);

        Map<String, String> expectedTemplateVars = new HashMap<>();
        expectedTemplateVars.put(NOTIFICATION_REFERENCE_KEY, (String) caseData.get(D_8_CASE_REFERENCE));
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_FIRST_NAME));
        expectedTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_LAST_NAME));

        DefaultTaskContext context = new DefaultTaskContext();

        Map<String, Object> returnedPayload = sendPetitionerCoRespondentRespondedNotificationEmail.execute(context, caseData);

        assertThat(caseData, is(sameInstance(returnedPayload)));
    }
}
