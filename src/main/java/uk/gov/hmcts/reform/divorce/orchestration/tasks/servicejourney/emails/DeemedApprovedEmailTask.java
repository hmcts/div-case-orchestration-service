package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.EmailTaskHelper.citizenTemplateVariables;

@Component
@Slf4j
public class DeemedApprovedEmailTask extends SendEmailTask {

    private static final String SUBJECT = "Your ‘deemed service’ application has been approved";

    public DeemedApprovedEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(Map<String, Object> caseData) {
        return SUBJECT;
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext taskContext, Map<String, Object> caseData) {
        return citizenTemplateVariables(caseData);
    }

    @Override
    protected EmailTemplateNames getTemplate(Map<String, Object> caseData) {
        return EmailTemplateNames.CITIZEN_DEEMED_APPROVED;
    }
}
