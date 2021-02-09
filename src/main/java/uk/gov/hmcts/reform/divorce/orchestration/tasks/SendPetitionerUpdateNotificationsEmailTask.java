package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.config.TemplateConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOT_RECEIVED_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOT_RECEIVED_AOS_STARTED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TEMPLATE_RELATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Component
@Slf4j
@RequiredArgsConstructor
public class SendPetitionerUpdateNotificationsEmailTask implements Task<Map<String, Object>> {

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

    public static final String RESP_ANSWER_RECVD_EVENT = "answerReceived";
    public static final String RESP_ANSWER_NOT_RECVD_EVENT = "answerNotReceived";

    private final EmailService emailService;
    private final TemplateConfig templateConfig;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        final String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
        final String eventId = context.getTransientObject(CASE_EVENT_ID_JSON_KEY);

        log.info(
            "CaseId: {} SendPetitionerUpdateNotificationsEmailTask is going to be executed for event {}",
            caseId,
            eventId
        );

        String petEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);
        String petSolEmail = (String) caseData.get(PETITIONER_SOLICITOR_EMAIL);

        String petitionerFirstName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_FIRST_NAME);
        String petitionerLastName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_LAST_NAME);
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(caseData);

        Map<String, String> templateVars = new HashMap<>();

        if (StringUtils.isNotBlank(petSolEmail)) {
            String respFirstName = getMandatoryPropertyValueAsString(caseData, RESP_FIRST_NAME_CCD_FIELD);
            String respLastName = getMandatoryPropertyValueAsString(caseData, RESP_LAST_NAME_CCD_FIELD);
            String solicitorName = getMandatoryPropertyValueAsString(caseData, PETITIONER_SOLICITOR_NAME);

            templateVars.put(NOTIFICATION_EMAIL, petSolEmail);
            templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, context.getTransientObject(CASE_ID_JSON_KEY));
            templateVars.put(NOTIFICATION_PET_NAME, petitionerFirstName + " " + petitionerLastName);
            templateVars.put(NOTIFICATION_RESP_NAME, respFirstName + " " + respLastName);
            templateVars.put(NOTIFICATION_SOLICITOR_NAME, solicitorName);

            try {
                sendSolicitorEmail(petSolEmail, eventId, templateVars, languagePreference);
            } catch (NotificationClientException e) {
                log.error("Error sending AOS overdue notification email to solicitor", e);
                throw new TaskException(e.getMessage(), e);
            }
        } else if (StringUtils.isNotBlank(petEmail)) {
            String relationship = getMandatoryPropertyValueAsString(caseData, D_8_DIVORCED_WHO);
            String welshRelationship =
                templateConfig.getTemplate().get(TEMPLATE_RELATION)
                    .get(LanguagePreference.WELSH).get(relationship);
            templateVars.put(NOTIFICATION_EMAIL, petEmail);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, petitionerFirstName);
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, petitionerLastName);
            templateVars.put(NOTIFICATION_RELATIONSHIP_KEY, relationship);
            templateVars.put(NOTIFICATION_WELSH_RELATIONSHIP_KEY, welshRelationship);
            templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE));

            try {
                sendPetitionerEmail(caseData, petEmail, eventId, templateVars, languagePreference);
            } catch (NotificationClientException e) {
                log.error("Error sending AOS overdue notification email to petitioner", e);
                throw new TaskException(e.getMessage(), e);
            }
        }
        return caseData;
    }

    private void sendSolicitorEmail(String petSolicitorEmail, String eventId, Map<String, String> templateVars,
                                    LanguagePreference languagePreference) throws NotificationClientException {
        final String caseId = templateVars.get(NOTIFICATION_CCD_REFERENCE_KEY);

        log.info("CaseId: {} email is going to be send to pet solicitor", caseId);

        if (StringUtils.equalsIgnoreCase(eventId, RESP_ANSWER_RECVD_EVENT)) {
            log.info(
                "CaseId: {} event = {}, sending {}",
                caseId,
                RESP_ANSWER_RECVD_EVENT,
                EmailTemplateNames.SOL_APPLICANT_AOS_RECEIVED.name()
            );
            emailService.sendEmailAndReturnExceptionIfFails(petSolicitorEmail,
                EmailTemplateNames.SOL_APPLICANT_AOS_RECEIVED.name(),
                templateVars,
                SOL_APPLICANT_AOS_RECEIVED_EMAIL_DESC,
                languagePreference);
        } else if (isAosOverdueEvent(eventId)) {
            log.info(
                "CaseId: {} AOS overdue, sending {}",
                caseId,
                EmailTemplateNames.SOL_APPLICANT_RESP_NOT_RESPONDED.name()
            );
            emailService.sendEmailAndReturnExceptionIfFails(petSolicitorEmail,
                EmailTemplateNames.SOL_APPLICANT_RESP_NOT_RESPONDED.name(),
                templateVars,
                SOL_APPLICANT_AOS_NOT_RECEIVED_EMAIL_DESC,
                languagePreference);
        } else {
            log.info("CaseId: {} generic email is going to be sent to solicitor", caseId);
            emailService.sendEmailAndReturnExceptionIfFails(
                petSolicitorEmail,
                EmailTemplateNames.SOL_GENERAL_CASE_UPDATE.name(),
                templateVars,
                SOL_GENERIC_UPDATE_EMAIL_DESC,
                languagePreference);
        }
    }

    private void sendPetitionerEmail(Map<String, Object> caseData, String petitionerEmail,
                                     String eventId, Map<String, String> templateVars,
                                     LanguagePreference languagePreference) throws NotificationClientException {
        final String caseRef = templateVars.get(NOTIFICATION_CCD_REFERENCE_KEY);

        log.info("CaseRef: {} email is going to be send to petitioner", caseRef);

        if (isAosOverdueEvent(eventId)) {
            log.info(
                "CaseRef: {} event = {}, email {}",
                caseRef,
                eventId,
                EmailTemplateNames.PETITIONER_RESP_NOT_RESPONDED.name()
            );
            emailService.sendEmailAndReturnExceptionIfFails(petitionerEmail,
                EmailTemplateNames.PETITIONER_RESP_NOT_RESPONDED.name(),
                templateVars,
                APPLICANT_AOS_NOT_RECEIVED_EMAIL_DESC,
                languagePreference);
        } else if (StringUtils.equalsIgnoreCase(eventId, RESP_ANSWER_RECVD_EVENT)) {
            log.info("CaseRef: {} event = {}, AosAnswerRecvdPet", caseRef, eventId);
            sendAosAnswerRecvdPetEmail(caseData, petitionerEmail, templateVars, languagePreference);
        } else {
            log.info("CaseRef: {} generic email is going to be sent to petitioner", caseRef);
            emailService.sendEmailAndReturnExceptionIfFails(
                petitionerEmail,
                EmailTemplateNames.GENERIC_UPDATE.name(),
                templateVars, GENERIC_UPDATE_EMAIL_DESC,
                languagePreference);
        }
    }

    private void sendAosAnswerRecvdPetEmail(Map<String, Object> caseData, String petitionerEmail,
                                            Map<String, String> templateVars,
                                            LanguagePreference languagePreference)
        throws NotificationClientException {
        final String caseRef = templateVars.get(NOTIFICATION_CCD_REFERENCE_KEY);

        if (isAdulteryAndNoConsent(caseData)) {
            if (isCoRespNamedAndNotReplied(caseData)) {
                log.info("CaseRef: {} adultery, co-resp not replied", caseRef);
                emailService.sendEmailAndReturnExceptionIfFails(petitionerEmail,
                    EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED.name(),
                    templateVars, AOS_RECEIVED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED_EMAIL_DESC,
                    languagePreference);
            } else {
                log.info("CaseRef: {} adultery, co-resp replied or not named", caseRef);
                emailService.sendEmailAndReturnExceptionIfFails(petitionerEmail,
                    EmailTemplateNames.AOS_RECEIVED_NO_ADMIT_ADULTERY.name(),
                    templateVars, AOS_RECEIVED_NO_ADMIT_ADULTERY_EMAIL_DESC,
                    languagePreference);
            }
        } else if (isSep2YrAndNoConsent(caseData)) {
            log.info("CaseRef: {} email = Sep2YrAndNoConsent", caseRef);
            emailService.sendEmailAndReturnExceptionIfFails(petitionerEmail,
                EmailTemplateNames.AOS_RECEIVED_NO_CONSENT_2_YEARS.name(),
                templateVars, AOS_RECEIVED_NO_CONSENT_2_YEARS_EMAIL_DESC,
                languagePreference);
        } else {
            log.info("CaseRef: {} generic email for AosAnswerRecvd is going to be sent to petitioner", caseRef);
            emailService.sendEmailAndReturnExceptionIfFails(
                petitionerEmail,
                EmailTemplateNames.GENERIC_UPDATE.name(),
                templateVars, GENERIC_UPDATE_EMAIL_DESC,
                languagePreference);
        }
    }

    private boolean isAdulteryAndNoConsent(Map<String, Object> caseData) {
        String reasonForDivorce = getFieldAsStringOrNull(caseData, D_8_REASON_FOR_DIVORCE);
        String respAdmitOrConsentToFact = getFieldAsStringOrNull(caseData, RESP_ADMIT_OR_CONSENT_TO_FACT);
        return StringUtils.equalsIgnoreCase(ADULTERY.getValue(), reasonForDivorce) && StringUtils.equalsIgnoreCase(NO_VALUE,
            respAdmitOrConsentToFact);
    }

    private boolean isAosOverdueEvent(String eventId) {
        return StringUtils.equalsIgnoreCase(eventId, RESP_ANSWER_NOT_RECVD_EVENT)
            || StringUtils.equalsIgnoreCase(eventId, NOT_RECEIVED_AOS_EVENT_ID)
            || StringUtils.equalsIgnoreCase(eventId, NOT_RECEIVED_AOS_STARTED_EVENT_ID);
    }

    private boolean isSep2YrAndNoConsent(Map<String, Object> caseData) {
        String reasonForDivorce = getFieldAsStringOrNull(caseData, D_8_REASON_FOR_DIVORCE);
        String respAdmitOrConsentToFact = getFieldAsStringOrNull(caseData, RESP_ADMIT_OR_CONSENT_TO_FACT);
        return SEPARATION_TWO_YEARS.getValue().equalsIgnoreCase(reasonForDivorce) && NO_VALUE.equalsIgnoreCase(respAdmitOrConsentToFact);
    }

    private boolean isCoRespNamedAndNotReplied(Map<String, Object> caseData) {
        String isCoRespNamed = getFieldAsStringOrNull(caseData, D_8_CO_RESPONDENT_NAMED);
        String receivedAosFromCoResp = getFieldAsStringOrNull(caseData, RECEIVED_AOS_FROM_CO_RESP);
        return StringUtils.equalsIgnoreCase(isCoRespNamed, YES_VALUE) && !StringUtils.equalsIgnoreCase(receivedAosFromCoResp, YES_VALUE);
    }

    private String getFieldAsStringOrNull(final Map<String, Object> caseData, String fieldKey) {
        Object fieldValue = caseData.get(fieldKey);
        if (fieldValue == null) {
            return null;
        }
        return fieldValue.toString();
    }
}