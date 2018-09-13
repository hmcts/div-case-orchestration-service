package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailToSend;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private EmailClient emailClient;

    @Value("#{${uk.gov.notify.email.templates}}")
    private Map<String, String> emailTemplates;

    @Value("#{${uk.gov.notify.email.template.vars}}")
    private Map<String, Map<String, String>> emailTemplateVars;

    public void sendEmail(EmailTemplateNames emailTemplate, String destinationAddress,
                                         Map<String, String> templateVars) throws NotificationClientException {

        String templateName = emailTemplate.name();
        EmailToSend emailToSend  = generateEmail(destinationAddress, templateName, templateVars);

        sendEmail(emailToSend, "submission notification");
    }

    private void sendEmail(EmailToSend emailToSend, String emailDescription) throws NotificationClientException {
        log.debug("Attempting to send {} email. Reference ID: {}", emailDescription, emailToSend.getReferenceId());
        emailClient.sendEmail(
                emailToSend.getTemplateId(),
                emailToSend.getEmailAddress(),
                emailToSend.getTemplateFields(),
                emailToSend.getReferenceId()
        );
        log.info("Sending email success. Reference ID: {}", emailToSend.getReferenceId());
    }

    private EmailToSend generateEmail(String destinationAddress,
                                      String templateName,
                                      Map<String, String> templateVars) {
        String              referenceId  = UUID.randomUUID().toString();
        String              templateId   = emailTemplates.get(templateName);
        Map<String, String> templateFlds = (templateVars != null ? templateVars : emailTemplateVars.get(templateName));

        return new EmailToSend(destinationAddress, templateId, templateFlds, referenceId);
    }

}

