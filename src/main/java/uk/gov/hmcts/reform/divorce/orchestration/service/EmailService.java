package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailToSend;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMAIL_ERROR_KEY;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private EmailClient emailClient;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    public Map<String, Object> sendEmail(String destinationAddress,
                                         String templateName,
                                         Map<String, String> templateVars,
                                         String emailDescription) {

        EmailToSend emailToSend = generateEmail(destinationAddress, templateName, templateVars);
        return sendEmailAndReturnErrorsInResponse(emailToSend, emailDescription);
    }

    public void sendEmailAndReturnExceptionIfFails(String destinationAddress,
                          String templateName,
                          Map<String, String> templateVars,
                          String emailDescription) throws NotificationClientException {

        EmailToSend emailToSend = generateEmail(destinationAddress, templateName, templateVars);
        sendEmailUsingClient(emailToSend, emailDescription);
    }

    private EmailToSend generateEmail(String destinationAddress,
                                      String templateName,
                                      Map<String, String> templateVars) {
        String referenceId = UUID.randomUUID().toString();
        String templateId = emailTemplatesConfig.getTemplates().get(templateName);
        Map<String, String> templateFields = (templateVars != null ? templateVars :
            emailTemplatesConfig.getTemplateVars().get(templateName));

        return new EmailToSend(destinationAddress, templateId, templateFields, referenceId);
    }

    private Map<String, Object> sendEmailAndReturnErrorsInResponse(EmailToSend emailToSend, String emailDescription) {
        Map<String, Object> response = new HashMap<>();
        try {
            sendEmailUsingClient(emailToSend, emailDescription);
        } catch (NotificationClientException e) {
            log.warn("Failed to send email. Reference ID: {}. Reason: {}", emailToSend.getReferenceId(),
                e.getMessage(), e);
            response.put(EMAIL_ERROR_KEY, e);
        }

        return response;
    }

    private void sendEmailUsingClient(EmailToSend emailToSend, String emailDescription) throws NotificationClientException {
        log.debug("Attempting to send {} email. Reference ID: {}", emailDescription, emailToSend.getReferenceId());
        emailClient.sendEmail(
            emailToSend.getTemplateId(),
            emailToSend.getDestinationEmailAddress(),
            emailToSend.getTemplateFields(),
            emailToSend.getReferenceId()
        );
        log.info("Sending email success. Reference ID: {}", emailToSend.getReferenceId());
    }
}