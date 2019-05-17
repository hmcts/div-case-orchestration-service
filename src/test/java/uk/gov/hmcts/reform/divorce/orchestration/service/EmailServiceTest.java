package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.NotificationServiceEmailTemplate;
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

    @Value("#{${uk.gov.notify.email.template.vars}}")
    private Map<String, Map<String, String>> emailTemplateVars;

    @Test
    public void sendEmailForSubmissionConfirmationShouldCallTheEmailClientToSendAnEmail()
        throws NotificationClientException {
        emailService.sendEmail(EMAIL_ADDRESS,
            NotificationServiceEmailTemplate.APPLIC_SUBMISSION,
            null,
            "submission notification");

        verify(mockClient).sendEmail(
            eq(NotificationServiceEmailTemplate.APPLIC_SUBMISSION.getTemplateId()),
            eq(EMAIL_ADDRESS),
            eq(emailTemplateVars.get(NotificationServiceEmailTemplate.APPLIC_SUBMISSION)),
            anyString());
    }

    @Test
    public void sendEmailShouldNotPropagateNotificationClientException()
        throws NotificationClientException {
        doThrow(new NotificationClientException(new Exception("Exception inception")))
            .when(mockClient).sendEmail(anyString(), anyString(), eq(null), anyString());
        try {
            emailService.sendEmail(EMAIL_ADDRESS,
                NotificationServiceEmailTemplate.AOS_RECEIVED_NO_CONSENT_2_YEARS,
                null,
                "resp does not consent to 2 year separation update notification");
        } catch (Exception e) {
            fail();
        }
    }
}