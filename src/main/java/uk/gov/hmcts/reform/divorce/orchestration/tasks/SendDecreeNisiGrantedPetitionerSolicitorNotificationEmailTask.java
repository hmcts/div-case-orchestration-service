package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_PETITIONER_DECREE_NISI_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;

@Component
public class SendDecreeNisiGrantedPetitionerSolicitorNotificationEmailTask extends PetitionerSolicitorSendEmailTask {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class EmailMetadata {
        public static EmailTemplateNames TEMPLATE_ID = SOL_PETITIONER_DECREE_NISI_GRANTED;
    }

    @Autowired
    protected SendDecreeNisiGrantedPetitionerSolicitorNotificationEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(TaskContext context, Map<String, Object> caseData) {
        return format(
            "%s vs %s: %s",
            getPetitionerFullName(caseData),
            getRespondentFullName(caseData)
        );
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return EmailMetadata.TEMPLATE_ID;
    }
}