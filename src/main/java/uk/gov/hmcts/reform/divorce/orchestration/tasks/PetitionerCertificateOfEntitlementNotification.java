package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils.formatDateWithCustomerFacingFormat;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.CaseLinkedForHearingWorkflow.CASE_ID_KEY;

@Component
@Slf4j
public class PetitionerCertificateOfEntitlementNotification implements Task<Map<String, Object>> {

    private static final String EMAIL_DESCRIPTION = "petitioner notification - certificate of description";
    private static final Period PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT = Period.ofWeeks(2);

    private static final String PETITIONER_EMAIL_ADDRESS = "email address";
    private static final String CASE_NUMBER = "case number";
    private static final String FIRST_NAME = "first name";
    private static final String LAST_NAME = "last name";
    private static final String DATE_OF_HEARING = "date of hearing";
    private static final String LIMIT_DATE_TO_CONTACT_COURT = "limit date to contact court";
    private static final String COSTS_CLAIM_GRANTED = "costs claim granted";
    private static final String COSTS_CLAIM_NOT_GRANTED = "costs claim not granted";

    private static final String NOTIFICATION_OPTIONAL_TEXT_YES_VALUE = "yes";
    private static final String NOTIFICATION_OPTIONAL_TEXT_NO_VALUE = "no";

    @Autowired
    private TaskCommons taskCommons;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        log.info("Executing task to notify petitioner about certificate of entitlement. Case id: {}",
            context.getTransientObject(CASE_ID_KEY));

        String petitionerEmail = getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_EMAIL);
        String familyManReference = getMandatoryPropertyValueAsString(payload, D_8_CASE_REFERENCE);
        LocalDate dateOfHearing = LocalDate.parse(getMandatoryPropertyValueAsString(payload, DATE_OF_HEARING_CCD_FIELD),
            ofPattern(CCD_DATE_FORMAT));
        LocalDate limitDateToContactCourt = dateOfHearing.minus(PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT);

        Map<String, String> templateParameters = new HashMap<>();
        templateParameters.put(PETITIONER_EMAIL_ADDRESS, petitionerEmail);
        templateParameters.put(CASE_NUMBER, familyManReference);
        templateParameters.put(FIRST_NAME, getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_FIRST_NAME));
        templateParameters.put(LAST_NAME, getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_LAST_NAME));
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
            taskCommons.sendEmail(EmailTemplateNames.PETITIONER_CERTIFICATE_OF_ENTITLEMENT_NOTIFICATION,
                EMAIL_DESCRIPTION,
                petitionerEmail,
                templateParameters);
            log.info("Petitioner notification sent for case {}", context.getTransientObject(CASE_ID_KEY));
        } catch (TaskException exception) {
            log.error("Failed to send petitioner notification for case {}", context.getTransientObject(CASE_ID_KEY));
            throw exception;
        } catch (Exception exception) {
            log.error("Failed to send petitioner notification for case {}", context.getTransientObject(CASE_ID_KEY));
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