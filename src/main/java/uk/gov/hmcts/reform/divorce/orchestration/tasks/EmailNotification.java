package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

@Component
public class EmailNotification  implements Task<Map<String, Object>> {

    private final EmailService emailService;

    @Autowired
    public EmailNotification(EmailService emailService) {
        this.emailService = emailService;
    }


    @Override
    public Map<String, Object> execute(TaskContext context,
                                       Map<String, Object> draft,
                                       Object... params) {
        return emailService.sendEmail(String.valueOf(params[0]));
    }
}