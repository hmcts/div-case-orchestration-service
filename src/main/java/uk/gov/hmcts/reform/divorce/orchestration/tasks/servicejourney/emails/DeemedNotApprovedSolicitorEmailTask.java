package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_DEEMED_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.FullNamesDataExtractor.getRespondentFullName;

@Component
@Slf4j
public class DeemedNotApprovedSolicitorEmailTask extends PetitionerSolicitorSendEmailTask {

    public DeemedNotApprovedSolicitorEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(TaskContext context, Map<String, Object> caseData) {
        return format(
            "%s vs %s: Deemed service application has been refused",
            getPetitionerFullName(caseData),
            getRespondentFullName(caseData)
        );
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return SOL_DEEMED_NOT_APPROVED;
    }
}
