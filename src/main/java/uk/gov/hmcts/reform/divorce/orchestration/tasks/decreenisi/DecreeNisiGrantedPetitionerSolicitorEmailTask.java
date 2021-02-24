package uk.gov.hmcts.reform.divorce.orchestration.tasks.decreenisi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_PETITIONER_DECREE_NISI_GRANTED;

@Component
public class DecreeNisiGrantedPetitionerSolicitorEmailTask extends PetitionerSolicitorSendEmailTask {

    @Autowired
    protected DecreeNisiGrantedPetitionerSolicitorEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return SOL_PETITIONER_DECREE_NISI_GRANTED;
    }
}