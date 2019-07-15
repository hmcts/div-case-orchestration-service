package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;

@Component
@Slf4j
public class DnSubmittedEmailNotificationTask implements Task<Map<String, Object>> {

    private final EmailService emailService;

    @Autowired
    public DnSubmittedEmailNotificationTask(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> data) {

        String petSolicitorEmail = (String) data.get(PET_SOL_EMAIL);

        String ccdReference = Objects.toString(data.get(D_8_CASE_REFERENCE), null);
        String petitionerFirstName = Objects.toString(data.get(D_8_PETITIONER_FIRST_NAME), null);
        String petitionerLastName = Objects.toString(data.get(D_8_PETITIONER_LAST_NAME), null);
        String petitionerEmail = Objects.toString(data.get(D_8_PETITIONER_EMAIL), null);

        Map<String, String> notificationTemplateVars = new HashMap<>();
        String template = null;
        String emailToBeSentTo = null;

        if (StringUtils.isNotBlank(petSolicitorEmail)) {
            String respFirstName = Objects.toString(data.get(RESP_FIRST_NAME_CCD_FIELD), null);
            String respLastName = Objects.toString(data.get(RESP_LAST_NAME_CCD_FIELD), null);
            String solicitorName = Objects.toString(data.get(PET_SOL_NAME), null);

            notificationTemplateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, ccdReference);
            notificationTemplateVars.put(NOTIFICATION_EMAIL, petSolicitorEmail);
            notificationTemplateVars.put(NOTIFICATION_PET_NAME, petitionerFirstName + " " + petitionerLastName);
            notificationTemplateVars.put(NOTIFICATION_RESP_NAME, respFirstName + " " + respLastName);
            notificationTemplateVars.put(NOTIFICATION_SOLICITOR_NAME, solicitorName);
            template = EmailTemplateNames.SOL_APPLICANT_DN_SUBMITTED.name();
            emailToBeSentTo = petSolicitorEmail;
        } else {
            notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, petitionerFirstName);
            notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, petitionerLastName);
            notificationTemplateVars.put(NOTIFICATION_REFERENCE_KEY, ccdReference);
            template = EmailTemplateNames.DN_SUBMISSION.name();
            emailToBeSentTo = petitionerEmail;
        }
        try {
            emailService.sendEmailAndReturnExceptionIfFails(emailToBeSentTo,
                template, notificationTemplateVars, "DN Submission");
        } catch (NotificationClientException e) {
            log.warn("Error sending email on DN submitted for case {}", ccdReference, e);
            context.setTransientObject(OrchestrationConstants.EMAIL_ERROR_KEY, e.getMessage());
            return Collections.emptyMap();
        }

        return data;
    }

}