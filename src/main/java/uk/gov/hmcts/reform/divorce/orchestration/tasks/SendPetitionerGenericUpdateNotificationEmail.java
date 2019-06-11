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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Component
public class SendPetitionerGenericUpdateNotificationEmail implements Task<Map<String, Object>> {

    private static final String EMAIL_DESC = "Generic Update Notification - Petitioner";

    private final EmailService emailService;

    @Autowired
    public SendPetitionerGenericUpdateNotificationEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        String petitionerEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);

        if (StringUtils.isNotBlank(petitionerEmail)) {
            Map<String, String> templateVars = new HashMap<>();

            String petitionerFirstName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_FIRST_NAME);
            String petitionerLastName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_LAST_NAME);
            String ccdReference = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);

            templateVars.put("email address", petitionerEmail);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, petitionerFirstName);
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, petitionerLastName);
            templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, ccdReference);

            emailService.sendEmail(petitionerEmail, EmailTemplateNames.GENERIC_UPDATE.name(), templateVars, EMAIL_DESC);
        }

        return caseData;
    }
}