package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.CITIZEN_DEEMED_APPROVED;

@Component
@Slf4j
public class DeemedApprovedPetitionerEmailTask extends PetitionerSendEmailTask {

    public DeemedApprovedPetitionerEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(TaskContext context, Map<String, Object> caseData) {
        return "Your ‘deemed service’ application has been approved";
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return CITIZEN_DEEMED_APPROVED;
    }
}
