package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.getRespondentSolicitorEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ServiceJourneyEmailTaskHelper.respondentSolicitorTemplateVariables;

public abstract class RespondentSolicitorSendEmailTask extends SendEmailTask {
    protected RespondentSolicitorSendEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext taskContext, Map<String, Object> caseData) {
        return respondentSolicitorTemplateVariables(taskContext, caseData);
    }

    @Override
    protected String getRecipientEmail(Map<String, Object> caseData) {
        return getRespondentSolicitorEmail(caseData);
    }
}
