package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.general;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.getCoRespondentEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.getNotRepresentedSubject;

public class GeneralEmailCoRespondent extends SendEmailTask {

    public GeneralEmailCoRespondent(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(Map<String, Object> caseData) {
        return getNotRepresentedSubject();
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext context, Map<String, Object> caseData) {
        return GeneralEmailTaskHelper.getExpectedNotificationTemplateVars(GeneralEmailTaskHelper.Party.CO_RESPONDENT, context, caseData);
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return EmailTemplateNames.GENERAL_EMAIL_CO_RESPONDENT;
    }

    @Override
    protected String getRecipientEmail(Map<String, Object> caseData) {
        return getCoRespondentEmail(caseData);
    }
}
