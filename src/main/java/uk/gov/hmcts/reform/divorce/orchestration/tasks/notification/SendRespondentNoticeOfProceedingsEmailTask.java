package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.RespondentSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_RESPONDENT_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ServiceJourneyEmailTaskHelper.respondentSolicitorWithOrgTemplateVariables;

@Component
@Slf4j
public class SendRespondentNoticeOfProceedingsEmailTask extends RespondentSolicitorSendEmailTask {

    public SendRespondentNoticeOfProceedingsEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext taskContext, Map<String, Object> caseData) {
        return respondentSolicitorWithOrgTemplateVariables(taskContext, caseData);
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return SOL_RESPONDENT_NOTICE_OF_PROCEEDINGS;
    }
}
