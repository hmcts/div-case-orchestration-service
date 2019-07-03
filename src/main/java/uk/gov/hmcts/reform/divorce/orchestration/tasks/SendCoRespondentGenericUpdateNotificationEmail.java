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

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Component
public class SendCoRespondentGenericUpdateNotificationEmail implements Task<Map<String, Object>> {

    private static final String EMAIL_DESC = "Generic Update Notification - CoRespondent";

    private final EmailService emailService;

    @Autowired
    public SendCoRespondentGenericUpdateNotificationEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        String coRespEmail = (String) caseData.get(CO_RESP_EMAIL_ADDRESS);

        if (StringUtils.isNotBlank(coRespEmail)) {

            String coRespFirstName = getMandatoryPropertyValueAsString(caseData, D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME);
            String coRespLastName =  getMandatoryPropertyValueAsString(caseData, D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME);
            String caseNumber =  getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);

            Map<String, String> templateVars = new HashMap<>();

            templateVars.put(NOTIFICATION_EMAIL, coRespEmail);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, coRespFirstName);
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, coRespLastName);
            templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, caseNumber);

            emailService.sendEmail(coRespEmail, EmailTemplateNames.GENERIC_UPDATE.name(), templateVars, EMAIL_DESC);
        }

        return caseData;
    }
}