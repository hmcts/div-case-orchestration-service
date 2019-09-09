package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Component
public class SendRespondentGenericUpdateNotificationEmail implements Task<Map<String, Object>> {

    private static final String RESP_GENERIC_EMAIL_DESC = "Generic Update Notification - Respondent";

    private final EmailService emailService;

    @Autowired
    public SendRespondentGenericUpdateNotificationEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        String respEmail = (String) caseData.get(RESPONDENT_EMAIL_ADDRESS);

        if (StringUtils.isNotBlank(respEmail)) {
            Map<String, String> templateVars = new HashMap<>();

            String respFirstName = getMandatoryPropertyValueAsString(caseData, RESP_FIRST_NAME_CCD_FIELD);
            String respLastName = getMandatoryPropertyValueAsString(caseData, RESP_LAST_NAME_CCD_FIELD);
            String caseNumber = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);

            templateVars.put(NOTIFICATION_EMAIL, respEmail);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, respFirstName);
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, respLastName);
            templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, caseNumber);

            emailService.sendEmail(respEmail, EmailTemplateNames.GENERIC_UPDATE_RESPONDENT.name(), templateVars, RESP_GENERIC_EMAIL_DESC);
        }

        return caseData;
    }
}