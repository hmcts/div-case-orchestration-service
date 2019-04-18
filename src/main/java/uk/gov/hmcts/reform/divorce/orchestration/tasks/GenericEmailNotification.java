package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Collections;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;

@Component
@Slf4j
public class GenericEmailNotification implements Task<Map<String, Object>> {

    private final EmailService emailService;

    @Autowired
    public GenericEmailNotification(EmailService emailService) {
        this.emailService = emailService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> execute(TaskContext context,
                                       Map<String, Object> data) {
        String emailAddress = String.valueOf(context.getTransientObject(NOTIFICATION_EMAIL));
        EmailTemplateNames template = (EmailTemplateNames) context.getTransientObject(NOTIFICATION_TEMPLATE);
        Map<String, String> templateVars = (Map<String, String>)context.getTransientObject(NOTIFICATION_TEMPLATE_VARS);

        try {
            emailService.sendEmailAndReturnExceptionIfFails(emailAddress, template.name(), templateVars,"submission notification");
        } catch (NotificationClientException e) {
            log.warn("Error sending email for case ID: " + context.getTransientObject(ID), e);
            context.setTransientObject(OrchestrationConstants.EMAIL_ERROR_KEY, e.getMessage());
            return Collections.emptyMap();
        }

        return data;
    }
}