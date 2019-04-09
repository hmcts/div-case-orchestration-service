package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
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

            templateVars.put("email address", petitionerEmail);
            templateVars.put("ref", (String) caseData.get(D_8_CASE_REFERENCE));
            templateVars.put("first name", (String) caseData.get(D_8_PETITIONER_FIRST_NAME));
            templateVars.put("last name", (String) caseData.get(D_8_PETITIONER_LAST_NAME));

            // Only send email to Petitioner when the respondent has not defended the case yet
            if (!YES_VALUE.equalsIgnoreCase((String) caseData.get(RESP_WILL_DEFEND_DIVORCE))) {
                if (YES_VALUE.equalsIgnoreCase((String) caseData.get(RECEIVED_AOS_FROM_RESP))) {
                    emailService.sendPetitionerEmailCoRespondentRespondWithAosNoDefend(petitionerEmail, templateVars);
                } else {
                    emailService.sendPetitionerEmailCoRespondentRespondWithAosNotStarted(petitionerEmail, templateVars);
                }
            }
        }

        return caseData;
    }

}