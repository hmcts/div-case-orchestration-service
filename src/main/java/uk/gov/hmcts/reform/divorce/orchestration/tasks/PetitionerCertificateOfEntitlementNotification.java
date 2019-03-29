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

import static java.lang.String.format;
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

    @Autowired
    private TaskCommons taskCommons;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        log.info("Executing task to notify petitioner about certificate of entitlement. Case id: {}",
            context.getTransientObject(CASE_ID_KEY));

        String petitionerEmail = getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_EMAIL);
        String familyManReference = getMandatoryPropertyValueAsString(payload, D_8_CASE_REFERENCE);
        String petitionerFullName = format("%s %s",
            getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_FIRST_NAME),
            getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_LAST_NAME));
        LocalDate dateOfHearing = LocalDate.parse(getMandatoryPropertyValueAsString(payload, DATE_OF_HEARING_CCD_FIELD),
            ofPattern(CCD_DATE_FORMAT));
        LocalDate limitDateToContactCourt = dateOfHearing.minus(PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT);

        Map<String, String> templateParameters = new HashMap<>();
        templateParameters.put("email address", petitionerEmail);
        templateParameters.put("family man reference", familyManReference);
        templateParameters.put("petitioner full name", petitionerFullName);
        templateParameters.put("date of hearing", formatDateWithCustomerFacingFormat(dateOfHearing));
        templateParameters.put("limit date to contact court",
            formatDateWithCustomerFacingFormat(limitDateToContactCourt));

        if (wasDivorceCostsClaimed(payload)) {
            if (wasCostsClaimGranted(payload)) {
                templateParameters.put("costs claim granted", "true");
            } else {
                templateParameters.put("costs claim not granted", "true");
            }
        }

        taskCommons.sendEmail(EmailTemplateNames.PETITIONER_CERTIFICATE_OF_ENTITLEMENT_NOTIFICATION,
            EMAIL_DESCRIPTION,
            petitionerEmail,
            templateParameters);

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