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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL_WITH_SPACE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;

@Component
public class SendRespondentGenericUpdateNotificationEmail implements Task<Map<String, Object>> {

    private static final String EMAIL_DESC = "Generic Update Notification - Respondent";

    private final EmailService emailService;

    @Autowired
    public SendRespondentGenericUpdateNotificationEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        String respEmail = (String) caseData.get(RESPONDENT_EMAIL_ADDRESS);
        String respFirstName = (String) caseData.get(RESP_FIRST_NAME_CCD_FIELD);
        String respLastName = (String)  caseData.get(RESP_LAST_NAME_CCD_FIELD);
        String caseNumber = (String) caseData.get(D_8_CASE_REFERENCE);

        if (StringUtils.isNotBlank(respEmail)) {

            Map<String, String> templateVars = new HashMap<>();

            templateVars.put(NOTIFICATION_EMAIL_WITH_SPACE, respEmail);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, respFirstName);
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, respLastName);
            templateVars.put(NOTIFICATION_CCD_REFERENCE, caseNumber);

            emailService.sendEmail(respEmail, EmailTemplateNames.GENERIC_UPDATE.name(), templateVars, EMAIL_DESC);
        }

        return caseData;
    }
}