package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.formatCaseIdToReferenceNumber;

@Component
public class SendRespondentSolicitorAosInvitationEmail implements Task<Map<String, Object>> {

    private final EmailService emailService;

    @Autowired
    public SendRespondentSolicitorAosInvitationEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {

        Map<String, String> templateVars = new HashMap<>();

        String caseId = (String) context.getTransientObject(CASE_ID_JSON_KEY);
        templateVars.put("CCD reference", formatCaseIdToReferenceNumber(caseId));
        templateVars.put("solicitors name", (String) payload.get(D8_RESPONDENT_SOLICITOR_NAME));
        templateVars.put("first name", (String) payload.get(RESP_FIRST_NAME_CCD_FIELD));
        templateVars.put("last name", (String) payload.get(RESP_LAST_NAME_CCD_FIELD));
        templateVars.put("access code", (String) context.getTransientObject(RESPONDENT_PIN));

        emailService.sendEmail(
                (String) payload.get(D8_RESPONDENT_SOLICITOR_EMAIL),
                EmailTemplateNames.RESPONDENT_SOLICITOR_AOS_INVITATION.name(),
                templateVars,
                "Respondent solicitor's AOS invitation"
        );

        return payload;
    }
}
