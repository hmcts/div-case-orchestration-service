package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
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

    @Value("#{${uk.gov.notify.email.templates}}")
    private Map<String, String> emailTemplates;

    @Value("#{${uk.gov.notify.email.template.vars}}")
    private Map<String, Map<String, String>> emailTemplateVars;

    public Map<String, Object> sendSaveDraftConfirmationEmail(String destinationAddress) {
        String      templateName = EmailTemplateNames.SAVE_DRAFT.name();
        EmailToSend emailToSend  = generateEmail(destinationAddress, templateName, null);

        return sendEmail(emailToSend, "draft saved confirmation");
    }

    public Map<String, Object> sendSubmissionNotificationEmail(String              destinationAddress,
                                                Map<String, String> templateVars) {
        String      templateName = EmailTemplateNames.APPLIC_SUBMISSION.name();
        EmailToSend emailToSend  = generateEmail(destinationAddress, templateName, templateVars);

        return sendEmail(emailToSend, "submission notification");
    }

    private EmailToSend generateEmail(String destinationAddress,
                                      String templateName,
                                      Map<String, String> templateVars) {
        String              referenceId  = UUID.randomUUID().toString();
        String              templateId   = emailTemplates.get(templateName);
        Map<String, String> templateFlds = (templateVars != null ? templateVars : emailTemplateVars.get(templateName));

        return new EmailToSend(destinationAddress, templateId, templateFlds, referenceId);
    }

    private Map<String, Object> sendEmail(EmailToSend emailToSend,
                           String      emailDescription) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.debug("Attempting to send {} email. Reference ID: {}", emailDescription, emailToSend.getReferenceId());
            emailClient.sendEmail(
                    emailToSend.getTemplateId(),
                    emailToSend.getEmailAddress(),
                    emailToSend.getTemplateFields(),
                    emailToSend.getReferenceId()
            );
            log.info("Sending email success. Reference ID: {}", emailToSend.getReferenceId());
        } catch (NotificationClientException e) {
            log.warn("Failed to send email. Reference ID: {}. Reason:", emailToSend.getReferenceId(), e);
            response.put(EMAIL_ERROR_KEY, e);
        }

        return response;
    }
}

