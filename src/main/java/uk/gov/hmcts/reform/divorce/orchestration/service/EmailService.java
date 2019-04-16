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
        String templateName = EmailTemplateNames.SAVE_DRAFT.name();
        EmailToSend emailToSend = generateEmail(destinationAddress, templateName, null);

        return sendEmailAndReturnErrorsInResponse(emailToSend, "draft saved confirmation");
    }

    public Map<String, Object> sendPetitionerSubmissionNotificationEmail(String destinationAddress,
                                                                         Map<String, String> templateVars) {
        String templateName = EmailTemplateNames.APPLIC_SUBMISSION.name();
        EmailToSend emailToSend = generateEmail(destinationAddress, templateName, templateVars);

        return sendEmailAndReturnErrorsInResponse(emailToSend, "submission notification");
    }

    public Map<String, Object> sendPetitionerGenericUpdateNotificationEmail(String destinationAddress,
                                                                            Map<String, String> templateVars) {
        String templateName = EmailTemplateNames.GENERIC_UPDATE.name();
        EmailToSend emailToSend = generateEmail(destinationAddress, templateName, templateVars);
        return sendEmailAndReturnErrorsInResponse(
                emailToSend,
                "generic update notification");
    }

    public Map<String, Object> sendPetitionerRespDoesNotAdmitAdulteryUpdateNotificationEmail(
            String destinationAddress,
            Map<String, String> templateVars) {
        String templateName = EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY.name();
        EmailToSend emailToSend = generateEmail(destinationAddress, templateName, templateVars);
        return sendEmailAndReturnErrorsInResponse(
                emailToSend,
                "resp does not admit adultery update notification");
    }

    public Map<String, Object> sendPetitionerRespDoesNotAdmitAdulteryCoRespNoReplyNotificationEmail(
        String destinationAddress,
        Map<String, String> templateVars) {
        String templateName = EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED.name();
        EmailToSend emailToSend = generateEmail(destinationAddress, templateName, templateVars);
        return sendEmailAndReturnErrorsInResponse(
               emailToSend,
               "resp does not admit adultery update notification - no reply from co-resp");
    }

    public Map<String, Object> sendPetitionerRespDoesNotConsent2YrsSepUpdateNotificationEmail(
            String destinationAddress,
            Map<String, String> templateVars) {
        String templateName = EmailTemplateNames.AOS_RECEIVED_NO_CONSENT_2_YEARS.name();
        EmailToSend emailToSend = generateEmail(destinationAddress, templateName, templateVars);
        return sendEmailAndReturnErrorsInResponse(
                emailToSend,
                "resp does not consent to 2 year separation update notification");
    }

    public Map<String, Object> sendPetitionerClarificationRequestEmail(final String destinationAddress, final Map<String, String> templateVars) {
        String templateName = EmailTemplateNames.PETITIONER_CLARIFICATION_REQUEST_EMAIL_NOTIFICATION.name();
        EmailToSend emailToSend = generateEmail(destinationAddress, templateName, templateVars);
        return sendEmailAndReturnErrorsInResponse(emailToSend,"clarification requested by LA from petitioner email notification");
    }

    public void sendEmail(EmailTemplateNames emailTemplate,
                          String destinationAddress,
                          Map<String, String> templateParameters) throws NotificationClientException {
        sendEmail(emailTemplate, "sendEmail", destinationAddress, templateParameters);
    }

    public void sendEmail(EmailTemplateNames emailTemplate,
                          String emailDescription,
                          String destinationAddress,
                          Map<String, String> templateParameters) throws NotificationClientException {

        String templateName = emailTemplate.name();
        EmailToSend emailToSend = generateEmail(destinationAddress, templateName, templateParameters);

        sendEmail(emailToSend, emailDescription);
    }

    private void sendEmail(EmailToSend emailToSend, String emailDescription) throws NotificationClientException {
        log.info("Attempting to send {} email. Reference ID: {}", emailDescription, emailToSend.getReferenceId());
        emailClient.sendEmail(
                emailToSend.getTemplateId(),
                emailToSend.getDestinationEmailAddress(),
                emailToSend.getTemplateFields(),
                emailToSend.getReferenceId()
        );
        log.info("Sending email success. Reference ID: {}", emailToSend.getReferenceId());
    }

    private EmailToSend generateEmail(String destinationAddress,
                                      String templateName,
                                      Map<String, String> templateVars) {
        String referenceId = UUID.randomUUID().toString();
        String templateId = emailTemplates.get(templateName);
        Map<String, String> templateFields = (templateVars != null
                ?
                templateVars
                :
                emailTemplateVars.get(templateName));

        return new EmailToSend(destinationAddress, templateId, templateFields, referenceId);
    }

    private Map<String, Object> sendEmailAndReturnErrorsInResponse(EmailToSend emailToSend, String emailDescription) {
        Map<String, Object> response = new HashMap<>();
        try {
            sendEmail(emailToSend, emailDescription);
        } catch (NotificationClientException e) {
            log.warn("Failed to send email. Reference ID: {}. Reason: {}", emailToSend.getReferenceId(),
                    e.getMessage(), e);
            response.put(EMAIL_ERROR_KEY, e);
        }

        return response;
    }
}
