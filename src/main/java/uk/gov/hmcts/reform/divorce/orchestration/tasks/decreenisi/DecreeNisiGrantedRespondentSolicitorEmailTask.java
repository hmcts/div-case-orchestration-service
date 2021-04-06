package uk.gov.hmcts.reform.divorce.orchestration.tasks.decreenisi;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.RespondentSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_RESPONDENT_DECREE_NISI_GRANTED;

@Component
public class DecreeNisiGrantedRespondentSolicitorEmailTask extends RespondentSolicitorSendEmailTask {

    protected DecreeNisiGrantedRespondentSolicitorEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return SOL_RESPONDENT_DECREE_NISI_GRANTED;
    }
}
