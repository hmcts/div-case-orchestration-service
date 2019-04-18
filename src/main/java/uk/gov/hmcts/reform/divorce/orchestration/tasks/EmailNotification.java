package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.LinkedHashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SEND_EMAIL;

@Component
public class EmailNotification  implements Task<Map<String, Object>> {

    private final EmailService emailService;

    @Autowired
    public EmailNotification(EmailService emailService) {
        this.emailService = emailService;
    }


    @Override
    public Map<String, Object> execute(TaskContext context,
                                       Map<String, Object> draft) {
        boolean sendEmail = parseBooleanFromString(String.valueOf(context.getTransientObject(NOTIFICATION_SEND_EMAIL)));
        String emailAddress = String.valueOf(context.getTransientObject(NOTIFICATION_EMAIL));
        if (sendEmail && StringUtils.isNotBlank(emailAddress)) {
            return emailService.sendEmail(emailAddress, EmailTemplateNames.SAVE_DRAFT.name(), null, "draft saved confirmation");
        }
        return new LinkedHashMap<>();
    }

    // For temporary backwards compatibility
    private boolean parseBooleanFromString(String email) {
        // Email is not just whitespace, is not null string (from String.valueOf) and not false
        return StringUtils.isNotBlank(email)
                && !"null".equalsIgnoreCase(email)
                && !Boolean.FALSE.toString().equalsIgnoreCase(email);
    }
}
