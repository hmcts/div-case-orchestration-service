package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils.formatDateWithCustomerFacingFormat;

@Component
public class SendDaGrantedNotificationEmail implements Task<Map<String, Object>> {

    private static final String EMAIL_DESC = "Decree Absolute Notification - Decree Absolute Granted";


    private final EmailService emailService;

    @Autowired
    public SendDaGrantedNotificationEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        sendEmail(
            getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_FIRST_NAME),
            getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_LAST_NAME),
            (String) caseData.get(D_8_PETITIONER_EMAIL),
            caseData
        );
        sendEmail(
            getMandatoryPropertyValueAsString(caseData, RESP_FIRST_NAME_CCD_FIELD),
            getMandatoryPropertyValueAsString(caseData, RESP_LAST_NAME_CCD_FIELD),
            (String) caseData.get(RESPONDENT_EMAIL_ADDRESS),
            caseData
        );

        return caseData;
    }

    public void sendEmail(String firstName, String lastName, String emailAddress, Map<String, Object> caseData) throws TaskException {

        if (StringUtils.isNotBlank(emailAddress)) {
            String ccdReference = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
            LocalDate daGrantedDate = LocalDate.parse((String) caseData.get(DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD));
            String daLimitDownloadDate = formatDateWithCustomerFacingFormat(daGrantedDate.plusYears(1));

            Map<String, String> templateVars = new HashMap<>();

            templateVars.put(NOTIFICATION_EMAIL_ADDRESS_KEY, emailAddress);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, firstName);
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, lastName);
            templateVars.put(NOTIFICATION_CASE_NUMBER_KEY, ccdReference);
            templateVars.put(NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE, daLimitDownloadDate);

            emailService.sendEmail(emailAddress, EmailTemplateNames.DA_GRANTED_NOTIFICATION.name(), templateVars, EMAIL_DESC);
        }
    }
}
