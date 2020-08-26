package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.CITIZEN_DEEMED_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_DEEMED_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.EmailTaskHelper.citizenTemplateVariables;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.EmailTaskHelper.solicitorTemplateVariables;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;

@Component
@Slf4j
public class DeemedNotApprovedEmailTask extends SendEmailTask {

    protected static String solicitorSubject = "%s vs %s: Deemed service application has been refused";
    protected static String citizenSubject = "Your ‘deemed service’ application has been refused";

    public DeemedNotApprovedEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(Map<String, Object> caseData) {
        return isPetitionerRepresented(caseData) ? solicitorSubject : citizenSubject;
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext taskContext, Map<String, Object> caseData) {
        return isPetitionerRepresented(caseData) ? solicitorTemplateVariables(taskContext, caseData) : citizenTemplateVariables(caseData);
    }

    @Override
    protected EmailTemplateNames getTemplate(Map<String, Object> caseData) {
        return isPetitionerRepresented(caseData) ? SOL_DEEMED_NOT_APPROVED : CITIZEN_DEEMED_NOT_APPROVED;
    }
}
