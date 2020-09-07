package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.CITIZEN_DEEMED_NOT_APPROVED;

@Component
@Slf4j
public class DeemedNotApprovedPetitionerEmailTask extends PetitionerSendEmailTask {
    protected static String subject = "Your ‘deemed service’ application has been refused";

    public DeemedNotApprovedPetitionerEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return CITIZEN_DEEMED_NOT_APPROVED;
    }
}
