package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.RespondentSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_RESPONDENT_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
public class SendRespondentNoticeOfProceedingsEmailTask extends RespondentSolicitorSendEmailTask {

    public SendRespondentNoticeOfProceedingsEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(TaskContext context, Map<String, Object> caseData) {
        return format(
            "CaseID: %s. Email to respondent solicitor on issue AOS event",
            getCaseId(context)
        );
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return SOL_RESPONDENT_NOTICE_OF_PROCEEDINGS;
    }
}
