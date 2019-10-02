package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_COMPANY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

@Component
@Slf4j
public class SendDaGrantedNotificationEmail implements Task<Map<String, Object>> {

    private static final String EMAIL_DESC = "Decree Absolute Notification - Decree Absolute Granted";
    private static final String SOL_EMAIL_DESC = "Decree Absolute Notification To Solicitor - Decree Absolute Granted";
    private static final String PETITIONER = "petitioner";
    private static final String RESPONDENT = "respondent";

    private final EmailService emailService;

    @Autowired
    public SendDaGrantedNotificationEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        String petEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);
        String respEmail = (String) caseData.get(RESPONDENT_EMAIL_ADDRESS);

        if (isSolicitorRepresentingPetitioner(caseData)) {
            String petSolEmail = getMandatoryPropertyValueAsString(caseData, PET_SOL_EMAIL);
            String petSolName = getMandatoryPropertyValueAsString(caseData, PET_SOL_NAME);

            sendEmailToSolicitor(context, caseData, petSolEmail, petSolName);
        } else if (!Strings.isNullOrEmpty(petEmail)) {
            sendEmailToPetitioner(caseData);
        }

        if (isSolicitorRepresentingRespondent(caseData)) {
            String respSolEmail = getMandatoryPropertyValueAsString(caseData, D8_RESPONDENT_SOLICITOR_EMAIL);
            String respSolName = getMandatoryPropertyValueAsString(caseData, D8_RESPONDENT_SOLICITOR_NAME);

            sendEmailToSolicitor(context, caseData, respSolEmail, respSolName);
        } else if (!Strings.isNullOrEmpty(respEmail)) {
            sendEmailToRespondent(caseData);
        }

        return caseData;
    }

    private void sendEmailToSolicitor(TaskContext context, Map<String, Object> caseData, String solEmail, String solName) throws TaskException {

        String ccdReference = context.getTransientObject(CASE_ID_JSON_KEY);
        String petFirstName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_FIRST_NAME);
        String petLastName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_LAST_NAME);
        String respFirstName = getMandatoryPropertyValueAsString(caseData, RESP_FIRST_NAME_CCD_FIELD);
        String respLastName = getMandatoryPropertyValueAsString(caseData, RESP_LAST_NAME_CCD_FIELD);

        Map<String, String> templateVars = new HashMap<>();

        templateVars.put(NOTIFICATION_EMAIL, solEmail);
        templateVars.put(NOTIFICATION_SOLICITOR_NAME, solName);
        templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, ccdReference);
        templateVars.put(NOTIFICATION_PET_NAME, petFirstName + " " + petLastName);
        templateVars.put(NOTIFICATION_RESP_NAME, respFirstName + " " + respLastName);

        emailService.sendEmail(
                solEmail,
                EmailTemplateNames.SOL_DA_GRANTED_NOTIFICATION.name(),
                templateVars,
                SOL_EMAIL_DESC);
    }

    private void sendEmailToPetitioner(Map<String, Object> caseData) throws TaskException {

        sendEmail(
                getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_FIRST_NAME),
                getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_LAST_NAME),
                getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_EMAIL),
                PETITIONER,
                caseData,
                getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE)
        );
    }


    private void sendEmailToRespondent(Map<String, Object> caseData) throws TaskException {
        sendEmail(
                getMandatoryPropertyValueAsString(caseData, RESP_FIRST_NAME_CCD_FIELD),
                getMandatoryPropertyValueAsString(caseData, RESP_LAST_NAME_CCD_FIELD),
                getMandatoryPropertyValueAsString(caseData, RESPONDENT_EMAIL_ADDRESS),
                RESPONDENT,
                caseData,
                getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE)
        );
    }

    public void sendEmail(String firstName, String lastName, String emailAddress,
                          String user, Map<String, Object> caseData, String ccdReference) {

        String daGrantedDataCcdField = (String) caseData.get(DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD);
        LocalDate daGrantedDate = LocalDateTime.parse(daGrantedDataCcdField).toLocalDate();
        String daLimitDownloadDate = formatDateWithCustomerFacingFormat(daGrantedDate.plusYears(1));

        if (StringUtils.isNotBlank(emailAddress)) {
            Map<String, String> templateVars = new HashMap<>();

            templateVars.put(NOTIFICATION_EMAIL_ADDRESS_KEY, emailAddress);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, firstName);
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, lastName);
            templateVars.put(NOTIFICATION_CASE_NUMBER_KEY, ccdReference);
            templateVars.put(NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE, daLimitDownloadDate);

            emailService.sendEmail(
                    emailAddress,
                    EmailTemplateNames.DA_GRANTED_NOTIFICATION.name(),
                    templateVars,
                    EMAIL_DESC);
        } else {
            log.warn("No {} email present for case reference: {}", user, ccdReference);
        }
    }

    private boolean isSolicitorRepresentingPetitioner(Map<String, Object> caseData) {
        final String petSolicitorEmail = (String) caseData.get(PET_SOL_EMAIL);

        return !Strings.isNullOrEmpty(petSolicitorEmail);
    }

    private boolean isSolicitorRepresentingRespondent(Map<String, Object> caseData) {
        final String respSolRepresented = (String) caseData.get(RESP_SOL_REPRESENTED);

        // temporary fix until we implement setting respondentSolicitorRepresented from CCD for RespSols
        final String respondentSolicitorName = (String) caseData.get(D8_RESPONDENT_SOLICITOR_NAME);
        final String respondentSolicitorCompany = (String) caseData.get(D8_RESPONDENT_SOLICITOR_COMPANY);

        return YES_VALUE.equalsIgnoreCase(respSolRepresented)
                || respondentSolicitorName != null && respondentSolicitorCompany != null;
    }
}