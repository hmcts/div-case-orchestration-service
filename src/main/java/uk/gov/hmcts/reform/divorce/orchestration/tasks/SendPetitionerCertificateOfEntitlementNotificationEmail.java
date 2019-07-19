package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_CLAIM_NOT_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LIMIT_DATE_TO_CONTACT_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OPTIONAL_TEXT_NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OPTIONAL_TEXT_YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils.formatDateWithCustomerFacingFormat;

@Component
@Slf4j
public class SendPetitionerCertificateOfEntitlementNotificationEmail implements Task<Map<String, Object>> {

    private static final String EMAIL_DESCRIPTION = "Petitioner Notification - Certificate of Entitlement";

    @Autowired
    private TaskCommons taskCommons;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
        log.info("Executing task to notify petitioner about certificate of entitlement. Case id: {}",
            caseId);

        String petSolicitorEmail = (String) payload.get(PET_SOL_EMAIL);
        String petitionerEmail = (String) payload.get(D_8_PETITIONER_EMAIL);
        String familyManCaseId = getMandatoryPropertyValueAsString(payload, D_8_CASE_REFERENCE);
        String petitionerFirstName = getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_FIRST_NAME);
        String petitionerLastName = getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_LAST_NAME);
        EmailTemplateNames template = null;
        String emailToBeSentTo = null;

        LocalDate dateOfHearing = CaseDataUtils.getLatestCourtHearingDateFromCaseData(payload);

        LocalDate limitDateToContactCourt = dateOfHearing.minus(PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT);

        Map<String, String> templateParameters = new HashMap<>();

        if (StringUtils.isNotBlank(petSolicitorEmail)) {
            String respFirstName = getMandatoryPropertyValueAsString(payload, RESP_FIRST_NAME_CCD_FIELD);
            String respLastName = getMandatoryPropertyValueAsString(payload, RESP_LAST_NAME_CCD_FIELD);
            String solicitorName = getMandatoryPropertyValueAsString(payload, PET_SOL_NAME);

            templateParameters.put(NOTIFICATION_CCD_REFERENCE_KEY, caseId);
            templateParameters.put(NOTIFICATION_EMAIL, petSolicitorEmail);
            templateParameters.put(NOTIFICATION_PET_NAME, petitionerFirstName + " " + petitionerLastName);
            templateParameters.put(NOTIFICATION_RESP_NAME, respFirstName + " " + respLastName);
            templateParameters.put(NOTIFICATION_SOLICITOR_NAME, solicitorName);
            template = EmailTemplateNames.SOL_APPLICANT_COE_NOTIFICATION;
            emailToBeSentTo = petSolicitorEmail;
        } else {
            templateParameters.put(NOTIFICATION_EMAIL, petitionerEmail);
            templateParameters.put(NOTIFICATION_CASE_NUMBER_KEY, familyManCaseId);
            templateParameters.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, petitionerFirstName);
            templateParameters.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, petitionerLastName);
            template = EmailTemplateNames.PETITIONER_CERTIFICATE_OF_ENTITLEMENT_NOTIFICATION;
            emailToBeSentTo = petitionerEmail;
        }
        templateParameters.put(DATE_OF_HEARING, formatDateWithCustomerFacingFormat(dateOfHearing));
        templateParameters.put(LIMIT_DATE_TO_CONTACT_COURT,
            formatDateWithCustomerFacingFormat(limitDateToContactCourt));

        if (wasDivorceCostsClaimed(payload)) {
            if (wasCostsClaimGranted(payload)) {
                templateParameters.put(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE);
                templateParameters.put(COSTS_CLAIM_NOT_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE);
            } else {
                templateParameters.put(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE);
                templateParameters.put(COSTS_CLAIM_NOT_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE);
            }
        } else {
            templateParameters.put(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE);
            templateParameters.put(COSTS_CLAIM_NOT_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE);
        }

        try {
            taskCommons.sendEmail(template,
                EMAIL_DESCRIPTION,
                emailToBeSentTo,
                templateParameters);
            log.info("Petitioner notification sent for case {}", (String) context.getTransientObject(CASE_ID_JSON_KEY));
        } catch (TaskException exception) {
            log.error("Failed to send petitioner notification for case {}", (String) context.getTransientObject(CASE_ID_JSON_KEY));
            throw exception;
        } catch (Exception exception) {
            log.error("Failed to send petitioner notification for case {}", (String) context.getTransientObject(CASE_ID_JSON_KEY));
            throw new TaskException(exception.getMessage(), exception);
        }

        return payload;
    }

    private Boolean wasDivorceCostsClaimed(Map<String, Object> payload) {
        return Optional.ofNullable(payload.get(DIVORCE_COSTS_CLAIM_CCD_FIELD))
            .map(String.class::cast)
            .map(YES_VALUE::equalsIgnoreCase)
            .orElse(false);
    }

    private boolean wasCostsClaimGranted(Map<String, Object> payload) {
        return Optional.ofNullable(payload.get(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD))
            .map(String.class::cast)
            .map(YES_VALUE::equalsIgnoreCase)
            .orElse(false);
    }

}