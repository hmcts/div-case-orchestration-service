package uk.gov.hmcts.reform.divorce.orchestration.tasks.aos;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

@Component
public class AosReceivedPetitionerSolicitorEmailTask extends PetitionerSolicitorSendEmailTask {

    public AosReceivedPetitionerSolicitorEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return EmailTemplateNames.SOL_APPLICANT_AOS_RECEIVED;
    }
}
