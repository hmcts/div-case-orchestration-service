package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.getPetitionerSolicitorEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ServiceJourneyEmailTaskHelper.solicitorTemplateVariables;

public abstract class PetitionerSolicitorSendEmailTask extends SendEmailTask {
    protected static String subject;

    public PetitionerSolicitorSendEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(Map<String, Object> caseData) {
        return format(subject, getPetitionerFullName(caseData), getRespondentFullName(caseData));
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
