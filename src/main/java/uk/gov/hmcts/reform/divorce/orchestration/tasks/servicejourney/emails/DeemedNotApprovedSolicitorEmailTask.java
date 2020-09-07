package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_DEEMED_NOT_APPROVED;

@Component
@Slf4j
public class DeemedNotApprovedSolicitorEmailTask extends PetitionerSolicitorSendEmailTask {
    protected static String subject = "%s vs %s: Deemed service application has been refused";

    public DeemedNotApprovedSolicitorEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return SOL_DEEMED_NOT_APPROVED;
    }
}
