package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.getPetitionerSolicitorEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ServiceJourneyEmailTaskHelper.solicitorTemplateVariables;

public abstract class PetitionerSolicitorSendEmailTask extends SendEmailTask {
    protected PetitionerSolicitorSendEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext taskContext, Map<String, Object> caseData) {
        return solicitorTemplateVariables(taskContext, caseData);
    }

    @Override
    protected String getRecipientEmail(Map<String, Object> caseData) {
        return getPetitionerSolicitorEmail(caseData);
    }
}
