package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailToSend;
import uk.gov.service.notify.NotificationClientException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.hibernate.validator.internal.util.StringHelper.format;
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
                                         LanguagePreference languagePreference) {
        EmailToSend emailToSend = generateEmail(destinationAddress, templateName, templateVars, languagePreference);
        return sendEmailAndReturnErrorsInResponse(emailToSend);
    }

    public Map<String, Object> sendEmail(String destinationAddress,
                                         String templateName,
                                         Map<String, String> templateVars,
                                         String emailDescription,
                                         LanguagePreference languagePreference) {
        log.warn("Please don't use this method for sending emails.");
        return sendEmail(destinationAddress, templateName, templateVars, languagePreference);
    }


    public void sendEmailAndReturnExceptionIfFails(String destinationAddress,
                                                   String templateName,
                                                   Map<String, String> templateVars,
                                                   String emailDescription,
                                                   LanguagePreference languagePreference) throws NotificationClientException {

        EmailToSend emailToSend = generateEmail(destinationAddress, templateName, templateVars, languagePreference);
        sendEmailUsingClient(emailToSend);
    }

    private EmailToSend generateEmail(String destinationAddress,
                                      String templateName,
                                      Map<String, String> templateVars,
                                      LanguagePreference languagePreference) {
        String referenceId = UUID.randomUUID().toString();

        String templateId = emailTemplatesConfig.getTemplates().get(languagePreference).get(templateName);
        Map<String, String> templateFields = (templateVars != null ? templateVars :
            emailTemplatesConfig.getTemplateVars().get(templateName));

        return new EmailToSend(destinationAddress, templateId, templateFields, referenceId);
    }

    private Map<String, Object> sendEmailAndReturnErrorsInResponse(EmailToSend emailToSend) {
        Map<String, Object> response = new HashMap<>();
        try {
            sendEmailUsingClient(emailToSend);
        } catch (NotificationClientException e) {
            log.warn(
                "Failed to send email. Reference ID: {}. Reason: {}",
                emailToSend.getReferenceId(), e.getMessage(), e
            );
            response.put(EMAIL_ERROR_KEY, e);
        }

        return response;
    }

    private void sendEmailUsingClient(EmailToSend emailToSend)
        throws NotificationClientException {
        logEmailDetails(emailToSend);

        emailClient.sendEmail(
            emailToSend.getTemplateId(),
            emailToSend.getDestinationEmailAddress(),
            emailToSend.getTemplateFields(),
            emailToSend.getReferenceId()
        );

        log.info(
            "Sent email {} successfully. Reference ID: {}",
            emailToSend.getTemplateId(), emailToSend.getReferenceId()
        );
    }

    private void logEmailDetails(EmailToSend emailToSend) {
        List<String> emailVarsSanitised = new ArrayList<>();

        emailToSend.getTemplateFields().forEach((key, value) -> {
            if (isEmpty(value)) {
                log.error(
                    "key: {} is empty for template {}, email ref.: {}",
                    key, emailToSend.getTemplateId(), emailToSend.getReferenceId()
                );
            }

            emailVarsSanitised.add(format("%s: string %s", key, value == null ? "NULL" : value.length()));
        });

        log.info(
            "Attempting to send {} email. Template vars passed: {}",
            emailToSend.getTemplateId(),
            String.join(", ", emailVarsSanitised)
        );
    }
}
