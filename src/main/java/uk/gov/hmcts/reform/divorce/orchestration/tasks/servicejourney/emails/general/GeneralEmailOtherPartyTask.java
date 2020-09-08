package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.general;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.getOtherPartyEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.getRepresentedSubject;

@Component
@Slf4j
public class GeneralEmailOtherPartyTask extends SendEmailTask {

    public GeneralEmailOtherPartyTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(TaskContext context, Map<String, Object> caseData) {
        return getRepresentedSubject(context, caseData);
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext context, Map<String, Object> caseData) {
        return GeneralEmailTaskHelper.getExpectedNotificationTemplateVars(GeneralEmailTaskHelper.Party.OTHER, context, caseData);
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return EmailTemplateNames.GENERAL_EMAIL_OTHER_PARTY;
    }

    @Override
    protected String getRecipientEmail(Map<String, Object> caseData) {
        return getOtherPartyEmail(caseData);
    }
}
