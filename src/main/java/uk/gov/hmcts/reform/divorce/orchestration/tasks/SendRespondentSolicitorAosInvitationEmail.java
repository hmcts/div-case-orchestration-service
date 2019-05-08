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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.formatCaseIdToReferenceNumber;

@Component
public class SendRespondentSolicitorAosInvitationEmail implements Task<Map<String, Object>> {

    private static final String ACCESS_CODE = "access code";
    private static final String SOLICITORS_NAME = "solicitors name";
    private static final String CCD_REFERENCE = "CCD reference";
    private static final String RESPONDENT_SOLICITOR_S_AOS_INVITATION = "Respondent solicitor's AOS invitation";

    private final EmailService emailService;

    @Autowired
    public SendRespondentSolicitorAosInvitationEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {

        Map<String, String> templateVars = new HashMap<>();

        String caseId = (String) context.getTransientObject(CASE_ID_JSON_KEY);
        templateVars.put(CCD_REFERENCE, formatCaseIdToReferenceNumber(caseId));
        templateVars.put(SOLICITORS_NAME, (String) payload.get(D8_RESPONDENT_SOLICITOR_NAME));
        templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, (String) payload.get(RESP_FIRST_NAME_CCD_FIELD));
        templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, (String) payload.get(RESP_LAST_NAME_CCD_FIELD));
        templateVars.put(ACCESS_CODE, (String) context.getTransientObject(RESPONDENT_PIN));

        emailService.sendEmail(
                (String) payload.get(D8_RESPONDENT_SOLICITOR_EMAIL),
                EmailTemplateNames.RESPONDENT_SOLICITOR_AOS_INVITATION.name(),
                templateVars,
                RESPONDENT_SOLICITOR_S_AOS_INVITATION
        );

        return payload;
    }
}
