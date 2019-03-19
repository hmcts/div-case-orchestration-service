package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailServiceTest {
    private static final String EMAIL_ADDRESS = "simulate-delivered@notifications.service.gov.uk";

    @MockBean
    private EmailClient mockClient;

    @Autowired
    private EmailService emailService;

    @Value("#{${uk.gov.notify.email.templates}}")
    private Map<String, String> emailTemplates;

    @Value("#{${uk.gov.notify.email.template.vars}}")
    private Map<String, Map<String, String>> emailTemplateVars;

    @Test
    public void sendSaveDraftConfirmationEmailShouldCallTheEmailClientToSendAnEmail()
        throws NotificationClientException {
        emailService.sendSaveDraftConfirmationEmail(EMAIL_ADDRESS);

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(EmailTemplateNames.SAVE_DRAFT.name())),
            eq(EMAIL_ADDRESS),
            eq(emailTemplateVars.get(EmailTemplateNames.SAVE_DRAFT.name())),
            anyString());
    }

    @Test
    public void sendSaveDraftConfirmationEmailShouldNotPropagateNotificationClientException()
        throws NotificationClientException {
        doThrow(new NotificationClientException(new Exception("Exception inception")))
            .when(mockClient).sendEmail(anyString(), anyString(), eq(null), anyString());

        try {
            emailService.sendSaveDraftConfirmationEmail(EMAIL_ADDRESS);
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    public void sendSubmissionConfirmationEmailShouldCallTheEmailClientToSendAnEmail()
        throws NotificationClientException {
        emailService.sendPetitionerSubmissionNotificationEmail(EMAIL_ADDRESS, null);

        verify(mockClient).sendEmail(
            eq(emailTemplates.get(EmailTemplateNames.APPLIC_SUBMISSION.name())),
            eq(EMAIL_ADDRESS),
            eq(emailTemplateVars.get(EmailTemplateNames.APPLIC_SUBMISSION.name())),
            anyString());
    }

    @Test
    public void sendSubmissionConfirmationEmailShouldNotPropagateNotificationClientException()
        throws NotificationClientException {
        doThrow(new NotificationClientException(new Exception("Exception inception")))
            .when(mockClient).sendEmail(anyString(), anyString(), eq(null), anyString());

        try {
            emailService.sendPetitionerSubmissionNotificationEmail(EMAIL_ADDRESS, null);
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    public void sendGenericUpdateEmailShouldCallTheEmailClientToSendAnEmail()
            throws NotificationClientException {
        emailService.sendPetitionerSubmissionNotificationEmail(EMAIL_ADDRESS, null);

        verify(mockClient).sendEmail(
                eq(emailTemplates.get(EmailTemplateNames.APPLIC_SUBMISSION.name())),
                eq(EMAIL_ADDRESS),
                eq(emailTemplateVars.get(EmailTemplateNames.APPLIC_SUBMISSION.name())),
                anyString());
    }

    @Test
    public void sendRespDoesNotAdmitAdulteryUpdateEmailShouldCallTheEmailClientToSendAnEmail()
            throws NotificationClientException {
        emailService.sendPetitionerRespDoesNotAdmitAdulteryUpdateNotificationEmail(EMAIL_ADDRESS, null);

        verify(mockClient).sendEmail(
                eq(emailTemplates.get(EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY.name())),
                eq(EMAIL_ADDRESS),
                eq(emailTemplateVars.get(EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY.name())),
                anyString());
    }
    
    @Test
    public void sendRespDoesNotAdmitAdulteryCoRespNoReplyUpdateShouldCallTheEmailClientToSendAnEmail()
            throws NotificationClientException {
        emailService.sendPetitionerRespDoesNotAdmitAdulteryCoRespNoReplyNotificationEmail(EMAIL_ADDRESS, null);

        verify(mockClient).sendEmail(
                eq(emailTemplates.get(EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED.name())),
                eq(EMAIL_ADDRESS),
                eq(emailTemplateVars.get(EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED.name())),
                anyString());
    }

    @Test
    public void sendRespDoesNotConsentTo2YearSepUpdateEmailShouldCallTheEmailClientToSendAnEmail()
            throws NotificationClientException {
        emailService.sendPetitionerRespDoesNotConsent2YrsSepUpdateNotificationEmail(EMAIL_ADDRESS, null);

        verify(mockClient).sendEmail(
                eq(emailTemplates.get(EmailTemplateNames.AOS_RECEIVED_NO_CONSENT_2_YEARS.name())),
                eq(EMAIL_ADDRESS),
                eq(emailTemplateVars.get(EmailTemplateNames.AOS_RECEIVED_NO_CONSENT_2_YEARS.name())),
                anyString());
    }

    @Test
    public void sendPetitionerClarificationRequestEmailShouldCallTheEmailClientToSendAnEmail() throws NotificationClientException {
        emailService.sendPetitionerClarificationRequestEmail(EMAIL_ADDRESS, null);

        verify(mockClient).sendEmail(
                eq(emailTemplates.get(EmailTemplateNames.PETITIONER_CLARIFICATION_REQUEST_EMAIL_NOTIFICATION.name())),
                eq(EMAIL_ADDRESS),
                eq(emailTemplateVars.get(EmailTemplateNames.PETITIONER_CLARIFICATION_REQUEST_EMAIL_NOTIFICATION.name())),
                anyString());
    }

}
