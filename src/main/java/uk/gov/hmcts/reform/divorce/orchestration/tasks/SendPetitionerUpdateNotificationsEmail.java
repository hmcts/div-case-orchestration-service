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

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCED_WHO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Component
public class SendPetitionerUpdateNotificationsEmail implements Task<Map<String, Object>> {

    private static final String GENERIC_UPDATE_EMAIL_DESC = "Generic Update Notification - Petitioner";
    private static final String AOS_RECEIVED_NO_ADMIT_ADULTERY_EMAIL_DESC =
            "Resp does not admit adultery update notification";
    private static final String AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED_EMAIL_DESC =
            "Resp does not admit adultery update notification - no reply from co-resp";
    private static final String AOS_RECEIVED_NO_CONSENT_2_YEARS_EMAIL_DESC =
            "Resp does not consent to 2 year separation update notification";
    private static final String SOL_APPLICANT_AOS_RECEIVED_EMAIL_DESC =
        "Resp response submission notification sent to solicitor";
    private static final String SOL_APPLICANT_AOS_NOT_RECEIVED_EMAIL_DESC =
        "Resp has not responded - notification sent to solicitor";
    private static final String APPLICANT_AOS_NOT_RECEIVED_EMAIL_DESC =
        "Resp has not responded - notification sent to petitioner";
    private static final String SOL_GENERIC_UPDATE_EMAIL_DESC =
        "Generic Update Notification - Petitioner solicitor";

    private static final String RESP_ANSWER_RECVD_EVENT = "answerReceived";
    private static final String RESP_ANSWER_NOT_RECVD_EVENT = "answerNotReceived";

    private final EmailService emailService;

    @Autowired
    public SendPetitionerUpdateNotificationsEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        String eventId = context.getTransientObject(CASE_EVENT_ID_JSON_KEY);
        String petEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);
        String petSolEmail = (String) caseData.get(PET_SOL_EMAIL);

        String petitionerFirstName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_FIRST_NAME);
        String petitionerLastName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_LAST_NAME);

        Map<String, String> templateVars = new HashMap<>();

        if (StringUtils.isNotBlank(petSolEmail)) {
            String respFirstName = getMandatoryPropertyValueAsString(caseData, RESP_FIRST_NAME_CCD_FIELD);
            String respLastName = getMandatoryPropertyValueAsString(caseData, RESP_LAST_NAME_CCD_FIELD);
            String solicitorName = getMandatoryPropertyValueAsString(caseData, PET_SOL_NAME);

            templateVars.put(NOTIFICATION_EMAIL, petSolEmail);
            templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, context.getTransientObject(CASE_ID_JSON_KEY));
            templateVars.put(NOTIFICATION_PET_NAME, petitionerFirstName + " " + petitionerLastName);
            templateVars.put(NOTIFICATION_RESP_NAME, respFirstName + " " + respLastName);
            templateVars.put(NOTIFICATION_SOLICITOR_NAME, solicitorName);

            sendSolicitorEmail(petSolEmail, eventId, templateVars);
        } else if (StringUtils.isNotBlank(petEmail)) {
            String relationship = getMandatoryPropertyValueAsString(caseData, D_8_DIVORCED_WHO);
            templateVars.put(NOTIFICATION_EMAIL, petEmail);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, petitionerFirstName);
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, petitionerLastName);
            templateVars.put(NOTIFICATION_RELATIONSHIP_KEY, relationship);
            templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE));

            sendPetitionerEmail(caseData, petEmail, eventId, templateVars);
        }
        return caseData;
    }

    private void sendSolicitorEmail(String petSolicitorEmail, String eventId, Map<String, String> templateVars) {
        if (StringUtils.equalsIgnoreCase(eventId, RESP_ANSWER_RECVD_EVENT)) {
            emailService.sendEmail(petSolicitorEmail,
                EmailTemplateNames.SOL_APPLICANT_AOS_RECEIVED.name(),
                templateVars,
                SOL_APPLICANT_AOS_RECEIVED_EMAIL_DESC);
        } else if (StringUtils.equalsIgnoreCase(eventId, RESP_ANSWER_NOT_RECVD_EVENT)) {
            emailService.sendEmail(petSolicitorEmail,
                EmailTemplateNames.SOL_APPLICANT_RESP_NOT_RESPONDED.name(),
                templateVars,
                SOL_APPLICANT_AOS_NOT_RECEIVED_EMAIL_DESC);
        } else {
            emailService.sendEmail(
                petSolicitorEmail,
                EmailTemplateNames.SOL_GENERAL_CASE_UPDATE.name(),
                templateVars,
                SOL_GENERIC_UPDATE_EMAIL_DESC);
        }
    }

    private void sendPetitionerEmail(Map<String, Object> caseData, String petitionerEmail,
                                     String eventId, Map<String, String> templateVars) {
        if (StringUtils.equalsIgnoreCase(eventId, RESP_ANSWER_NOT_RECVD_EVENT)) {
            emailService.sendEmail(petitionerEmail,
                EmailTemplateNames.PETITIONER_RESP_NOT_RESPONDED.name(),
                templateVars,
                APPLICANT_AOS_NOT_RECEIVED_EMAIL_DESC);
        } else if (StringUtils.equalsIgnoreCase(eventId, RESP_ANSWER_RECVD_EVENT)) {
            sendAosAnswerRecvdPetEmail(caseData, petitionerEmail, templateVars);
        } else {
            emailService.sendEmail(
                petitionerEmail,
                EmailTemplateNames.GENERIC_UPDATE.name(),
                templateVars, GENERIC_UPDATE_EMAIL_DESC);
        }
    }

    private void sendAosAnswerRecvdPetEmail(Map<String, Object> caseData, String petitionerEmail,
                                            Map<String, String> templateVars) {
        if (isAdulteryAndNoConsent(caseData)) {
            if (isCoRespNamedAndNotReplied(caseData)) {
                emailService.sendEmail(petitionerEmail,
                    EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED.name(),
                    templateVars, AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED_EMAIL_DESC);
            } else {
                emailService.sendEmail(petitionerEmail,
                    EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY.name(),
                    templateVars, AOS_RECEIVED_NO_ADMIT_ADULTERY_EMAIL_DESC);
            }
        } else if (isSep2YrAndNoConsent(caseData)) {
            emailService.sendEmail(petitionerEmail,
                EmailTemplateNames.AOS_RECEIVED_NO_CONSENT_2_YEARS.name(),
                templateVars, AOS_RECEIVED_NO_CONSENT_2_YEARS_EMAIL_DESC);

        } else {
            emailService.sendEmail(
                petitionerEmail,
                EmailTemplateNames.GENERIC_UPDATE.name(),
                templateVars, GENERIC_UPDATE_EMAIL_DESC);
        }
    }

    private boolean isAdulteryAndNoConsent(Map<String, Object> caseData) {
        String reasonForDivorce = getFieldAsStringOrNull(caseData, D_8_REASON_FOR_DIVORCE);
        String respAdmitOrConsentToFact = getFieldAsStringOrNull(caseData, RESP_ADMIT_OR_CONSENT_TO_FACT);
        return StringUtils.equalsIgnoreCase(ADULTERY, reasonForDivorce) && StringUtils.equalsIgnoreCase(NO_VALUE, respAdmitOrConsentToFact);
    }

    private boolean isSep2YrAndNoConsent(Map<String, Object> caseData) {
        String reasonForDivorce = getFieldAsStringOrNull(caseData, D_8_REASON_FOR_DIVORCE);
        String respAdmitOrConsentToFact = getFieldAsStringOrNull(caseData, RESP_ADMIT_OR_CONSENT_TO_FACT);
        return StringUtils.equalsIgnoreCase(SEPARATION_2YRS, reasonForDivorce) && StringUtils.equalsIgnoreCase(NO_VALUE, respAdmitOrConsentToFact);
    }

    private boolean isCoRespNamedAndNotReplied(Map<String, Object> caseData) {
        String isCoRespNamed = getFieldAsStringOrNull(caseData, D_8_CO_RESPONDENT_NAMED);
        String receivedAosFromCoResp = getFieldAsStringOrNull(caseData, RECEIVED_AOS_FROM_CO_RESP);
        return StringUtils.equalsIgnoreCase(isCoRespNamed, YES_VALUE) && !StringUtils.equalsIgnoreCase(receivedAosFromCoResp, YES_VALUE);
    }

    private String getFieldAsStringOrNull(final Map<String, Object>  caseData, String fieldKey) {
        Object fieldValue = caseData.get(fieldKey);
        if (fieldValue == null) {
            return null;
        }
        return fieldValue.toString();
    }
}