package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_DISPENSED_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ServiceJourneyEmailTaskHelper.solicitorTemplateVariables;

@Component
@Slf4j
public class DispensedNotApprovedSolicitorEmailTask extends SendEmailTask {
    protected static String SUBJECT = "%s vs %s: Solicitor dispensed application not approved";

    public DispensedNotApprovedSolicitorEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject() {
        return SUBJECT;
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext taskContext, Map<String, Object> caseData) {
        return solicitorTemplateVariables(taskContext, caseData);
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return SOL_DISPENSED_NOT_APPROVED;
    }
}
