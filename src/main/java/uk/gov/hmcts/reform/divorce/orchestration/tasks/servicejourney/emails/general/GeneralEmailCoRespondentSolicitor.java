package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.general;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.getCoRespondentSolicitorEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.getRepresentedSubject;

public class GeneralEmailCoRespondentSolicitor extends SendEmailTask {

    public GeneralEmailCoRespondentSolicitor(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(Map<String, Object> caseData) {
        return getRepresentedSubject(caseData);
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext context, Map<String, Object> caseData) {
        return GeneralEmailTaskHelper.getExpectedNotificationTemplateVars(GeneralEmailTaskHelper.Party.CO_RESPONDENT_SOLICITOR, context, caseData);
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return EmailTemplateNames.GENERAL_EMAIL_CO_RESPONDENT_SOLICITOR;
    }

    @Override
    protected String getRecipientEmail(Map<String, Object> caseData) {
        return getCoRespondentSolicitorEmail(caseData);
    }
}
