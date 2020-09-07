package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.CITIZEN_DISPENSED_APPROVED;

@Component
@Slf4j
public class DispensedApprovedPetitionerEmailTask extends PetitionerSolicitorSendEmailTask {
    protected static String subject = "Your ‘dispense with service’ application has been approved";

    public DispensedApprovedPetitionerEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return CITIZEN_DISPENSED_APPROVED;
    }
}
