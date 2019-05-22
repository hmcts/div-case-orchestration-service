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

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;

@Component
public class SendCoRespondentGenericUpdateNotificationEmail implements Task<Map<String, Object>> {

    private static final String EMAIL_DESC = "Generic Update Notification - CoRespondent";

    private final EmailService emailService;

    @Autowired
    public SendCoRespondentGenericUpdateNotificationEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        String coRespEmail = (String) caseData.get(CO_RESP_EMAIL_ADDRESS);
        String coRespFirstName = (String) caseData.get(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME);
        String coRespLastName = (String)  caseData.get(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME);
        String caseNumber = (String) caseData.get(D_8_CASE_REFERENCE);

        if (StringUtils.isNotBlank(coRespEmail)) {

            Map<String, String> templateVars = new HashMap<>();

            templateVars.put("email address", coRespEmail);
            templateVars.put("first name", coRespFirstName);
            templateVars.put("last name", coRespLastName);
            templateVars.put("CCD reference", caseNumber);

            emailService.sendEmail(coRespEmail, EmailTemplateNames.GENERIC_UPDATE.name(), templateVars, EMAIL_DESC);
        }

        return caseData;
    }
}