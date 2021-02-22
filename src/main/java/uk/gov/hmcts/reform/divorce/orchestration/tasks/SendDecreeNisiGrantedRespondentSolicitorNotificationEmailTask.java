package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_RESPONDENT_DECREE_NISI_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;

@Component
public class SendDecreeNisiGrantedRespondentSolicitorNotificationEmailTask extends PetitionerSolicitorSendEmailTask {

    private static final String RESP_SOL_EMAIL_DESC = "Decree Nisi granted - Solicitor (Respondent)";

    protected SendDecreeNisiGrantedRespondentSolicitorNotificationEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(TaskContext context, Map<String, Object> caseData) {
        return format(
            "%s vs %s: %s",
            getPetitionerFullName(caseData),
            getRespondentFullName(caseData),
            RESP_SOL_EMAIL_DESC
        );
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return SOL_RESPONDENT_DECREE_NISI_GRANTED;
    }
}