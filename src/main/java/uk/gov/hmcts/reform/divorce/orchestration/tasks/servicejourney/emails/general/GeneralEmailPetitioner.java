package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.general;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.getNotRepresentedSubject;

public class GeneralEmailPetitioner extends PetitionerSendEmailTask {

    public GeneralEmailPetitioner(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext taskContext, Map<String, Object> caseData) {
        return GeneralEmailTaskHelper.getExpectedNotificationTemplateVars(GeneralEmailTaskHelper.Party.PETITIONER, taskContext, caseData);
    }

    @Override
    protected String getSubject(Map<String, Object> caseData) {
        return getNotRepresentedSubject();
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return EmailTemplateNames.GENERAL_EMAIL_PETITIONER;
    }
}
