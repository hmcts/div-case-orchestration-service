package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_DEEMED_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.getPetitionerSolicitorEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ServiceJourneyEmailTaskHelper.solicitorTemplateVariables;

@Component
@Slf4j
public class DeemedNotApprovedSolicitorEmailTask extends SendEmailTask {
    protected static String SUBJECT = "%s vs %s: Deemed service application has been refused";

    public DeemedNotApprovedSolicitorEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(Map<String, Object> caseData) {
        return format(SUBJECT, getPetitionerFullName(caseData), getRespondentFullName(caseData));
    }

    @Override
    protected String getRecipientEmail(Map<String, Object> caseData) {
        return getPetitionerSolicitorEmail(caseData);
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext taskContext, Map<String, Object> caseData) {
        return solicitorTemplateVariables(taskContext, caseData);
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return SOL_DEEMED_NOT_APPROVED;
    }
}
