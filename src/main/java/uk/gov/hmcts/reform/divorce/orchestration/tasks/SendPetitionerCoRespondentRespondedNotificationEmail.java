package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class SendPetitionerCoRespondentRespondedNotificationEmail implements Task<Map<String, Object>> {

    private final EmailService emailService;

    @Autowired
    public SendPetitionerCoRespondentRespondedNotificationEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        String petitionerEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);

        if (StringUtils.isNotBlank(petitionerEmail)) {
            Map<String, String> templateVars = new HashMap<>();
            
            templateVars.put(NOTIFICATION_REFERENCE_KEY, (String) caseData.get(D_8_CASE_REFERENCE));
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_FIRST_NAME));
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_LAST_NAME));

            if (isRespondentRespondedCase(caseData)) {
                if (!isRespondentDefendedCase(caseData)) {
                    emailService.sendEmail(petitionerEmail,
                        EmailTemplateNames.APPLICANT_CO_RESPONDENT_RESPONDS_AOS_SUBMITTED_NO_DEFEND.name(),
                        templateVars,
                        "co-respondent responded when aos is undefended");
                }
            } else {
                emailService.sendEmail(petitionerEmail,
                    EmailTemplateNames.APPLICANT_CO_RESPONDENT_RESPONDS_AOS_NOT_SUBMITTED.name(),
                    templateVars,
                    "co-respondent responded but respondent has not");
            }
        }

        return caseData;
    }

    private boolean isRespondentDefendedCase(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(RESP_WILL_DEFEND_DIVORCE));
    }

    private boolean isRespondentRespondedCase(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(RECEIVED_AOS_FROM_RESP));
    }
}