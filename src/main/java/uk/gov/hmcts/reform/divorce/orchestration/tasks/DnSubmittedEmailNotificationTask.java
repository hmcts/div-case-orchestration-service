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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;

@Component
@Slf4j
public class DnSubmittedEmailNotificationTask implements Task<Map<String, Object>> {

    private final EmailService emailService;

    @Autowired
    public DnSubmittedEmailNotificationTask(EmailService emailService) {
        this.emailService = emailService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> data) {

        String caseId = Objects.toString(data.get(D_8_CASE_REFERENCE), null);
        String firstName = Objects.toString(data.get(D_8_PETITIONER_FIRST_NAME), null);
        String lastName = Objects.toString(data.get(D_8_PETITIONER_LAST_NAME), null);
        String emailAddress = Objects.toString(data.get(D_8_PETITIONER_EMAIL), null);

        Map<String, String> notificationTemplateVars = new HashMap<>();
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, firstName);
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, lastName);
        notificationTemplateVars.put(NOTIFICATION_REFERENCE_KEY, caseId);
        try {
            emailService.sendEmailAndReturnExceptionIfFails(emailAddress,
                EmailTemplateNames.DN_SUBMISSION.name(), notificationTemplateVars, "DN Submission");
        } catch (NotificationClientException e) {
            log.warn("Error sending email to {}", emailAddress, e);
            context.setTransientObject(OrchestrationConstants.EMAIL_ERROR_KEY, e.getMessage());
            return Collections.emptyMap();
        }

        return data;
    }

}