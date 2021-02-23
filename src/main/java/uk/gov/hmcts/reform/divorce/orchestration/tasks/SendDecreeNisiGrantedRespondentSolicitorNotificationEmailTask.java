package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.RespondentSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_RESPONDENT_DECREE_NISI_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;

@Component
public class SendDecreeNisiGrantedRespondentSolicitorNotificationEmailTask extends RespondentSolicitorSendEmailTask {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class EmailMetadata {
        public static final EmailTemplateNames TEMPLATE_ID = SOL_RESPONDENT_DECREE_NISI_GRANTED;
        public static final String SUBJECT_FORMAT = "%s vs %s: %s";
    }

    protected SendDecreeNisiGrantedRespondentSolicitorNotificationEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(TaskContext context, Map<String, Object> caseData) {
        return format(
            EmailMetadata.SUBJECT_FORMAT,
            getPetitionerFullName(caseData),
            getRespondentFullName(caseData)
        );
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return EmailMetadata.TEMPLATE_ID;
    }
}