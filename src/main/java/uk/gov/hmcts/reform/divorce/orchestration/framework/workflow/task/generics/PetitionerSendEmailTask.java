package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.getPetitionerEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ServiceJourneyEmailTaskHelper.citizenTemplateVariables;

public abstract class PetitionerSendEmailTask extends SendEmailTask {
    protected PetitionerSendEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext taskContext, Map<String, Object> caseData) {
        return citizenTemplateVariables(caseData);
    }

    @Override
    protected boolean canEmailBeSent(Map<String, Object> caseData) {
        return isPetitionerEmailPopulated(caseData);
    }

    @Override
    protected String getRecipientEmail(Map<String, Object> caseData) {
        return getPetitionerEmail(caseData);
    }

    private boolean isPetitionerEmailPopulated(Map<String, Object> caseData) {
        return !EmailDataExtractor.getPetitionerEmailOrEmpty(caseData).isEmpty();
    }
}
