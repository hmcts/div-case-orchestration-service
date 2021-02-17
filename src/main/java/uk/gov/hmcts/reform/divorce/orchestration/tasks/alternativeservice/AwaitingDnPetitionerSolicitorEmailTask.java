package uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

@Component
public class AwaitingDnPetitionerSolicitorEmailTask extends PetitionerSolicitorSendEmailTask {

    public AwaitingDnPetitionerSolicitorEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return EmailTemplateNames.PET_SOL_AWAITING_DN_SERVED_BY_PROCESS;
    }

    @Override
    protected String getSubject(TaskContext context, Map<String, Object> caseData) {
        return "Solicitor apply for DN - process server";
    }
}
