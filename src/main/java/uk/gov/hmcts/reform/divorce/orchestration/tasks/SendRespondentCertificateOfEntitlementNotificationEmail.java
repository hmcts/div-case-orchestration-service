package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_NAME_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LIMIT_DATE_TO_CONTACT_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OPTIONAL_TEXT_NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OPTIONAL_TEXT_YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

@Component
@AllArgsConstructor
@Slf4j
public class SendRespondentCertificateOfEntitlementNotificationEmail implements Task<Map<String, Object>> {

    private static final String EMAIL_DESCRIPTION = "Respondent Notification - Certificate of Entitlement";

    private TaskCommons taskCommons;
    private TemplateConfigService templateConfigService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseDataPayload) throws TaskException {
        log.info("Executing task to notify respondent about certificate of entitlement. Case id: {}",
            (String) context.getTransientObject(CASE_ID_JSON_KEY));

        String respSolEmail = (String) caseDataPayload.get(D8_RESPONDENT_SOLICITOR_EMAIL);
        String respondentEmail = (String) caseDataPayload.get(RESPONDENT_EMAIL_ADDRESS);
        String familyManCaseId = getMandatoryPropertyValueAsString(caseDataPayload, D_8_CASE_REFERENCE);
        String respondentFirstName = getMandatoryPropertyValueAsString(caseDataPayload, RESP_FIRST_NAME_CCD_FIELD);
        String respondentLastName = getMandatoryPropertyValueAsString(caseDataPayload, RESP_LAST_NAME_CCD_FIELD);
        String courtName = getMandatoryPropertyValueAsString(caseDataPayload, COURT_NAME_CCD_FIELD);


        LocalDate dateOfHearing = CaseDataUtils.getLatestCourtHearingDateFromCaseData(caseDataPayload);

        LocalDate limitDateToContactCourt = dateOfHearing.minus(PERIOD_BEFORE_HEARING_DATE_TO_CONTACT_COURT);

        Map<String, String> templateParameters = new HashMap<>();
        EmailTemplateNames template;
        String emailToBeSentTo;

        templateParameters.put(DATE_OF_HEARING, formatDateWithCustomerFacingFormat(dateOfHearing));
        templateParameters.put(LIMIT_DATE_TO_CONTACT_COURT,
            formatDateWithCustomerFacingFormat(limitDateToContactCourt));

        if (wasCostsClaimGranted(caseDataPayload)) {
            templateParameters.put(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE);
        } else {
            templateParameters.put(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE);
        }

        try {
            if (StringUtils.isNotBlank(respSolEmail)) {
                String petitionerFirstName = getMandatoryPropertyValueAsString(caseDataPayload, D_8_PETITIONER_FIRST_NAME);
                String petitionerLastName = getMandatoryPropertyValueAsString(caseDataPayload, D_8_PETITIONER_LAST_NAME);
                String solicitorName = (String) caseDataPayload.get(D8_RESPONDENT_SOLICITOR_NAME);
                String caseId = context.getTransientObject(CASE_ID_JSON_KEY);

                templateParameters.put(NOTIFICATION_PET_NAME, petitionerFirstName + " " + petitionerLastName);
                templateParameters.put(NOTIFICATION_RESP_NAME, respondentFirstName + " " + respondentLastName);
                templateParameters.put(NOTIFICATION_EMAIL, respSolEmail);
                templateParameters.put(NOTIFICATION_CCD_REFERENCE_KEY, caseId);
                templateParameters.put(NOTIFICATION_SOLICITOR_NAME, solicitorName);

                template = EmailTemplateNames.SOL_RESP_COE_NOTIFICATION;
                emailToBeSentTo = respSolEmail;
            } else {
                String petitionerInferredGender = getMandatoryPropertyValueAsString(caseDataPayload,
                    D_8_INFERRED_PETITIONER_GENDER);

                String petRelToRespondent = templateConfigService.getRelationshipTermByGender(petitionerInferredGender, LanguagePreference.ENGLISH);
                String welshPetRelToRespondent = templateConfigService.getRelationshipTermByGender(petitionerInferredGender,LanguagePreference.WELSH);
                templateParameters.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, respondentFirstName);
                templateParameters.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, respondentLastName);
                templateParameters.put(NOTIFICATION_EMAIL, respondentEmail);
                templateParameters.put(NOTIFICATION_CASE_NUMBER_KEY, familyManCaseId);
                templateParameters.put(NOTIFICATION_HUSBAND_OR_WIFE, petRelToRespondent);
                templateParameters.put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, welshPetRelToRespondent);

                template = EmailTemplateNames.RESPONDENT_CERTIFICATE_OF_ENTITLEMENT_NOTIFICATION;
                emailToBeSentTo = respondentEmail;
            }

            templateParameters.put(COURT_NAME_TEMPLATE_ID, taskCommons.getDnCourt(courtName).getName());
            Optional<LanguagePreference> languagePreference = CaseDataUtils.getLanguagePreference(
                caseDataPayload);

            taskCommons.sendEmail(template, EMAIL_DESCRIPTION, emailToBeSentTo, templateParameters, languagePreference);
            log.info("Respondent notification sent for case {}", (String) context.getTransientObject(CASE_ID_JSON_KEY));
        } catch (TaskException exception) {
            log.error("Failed to send Respondent notification for case {}", (String) context.getTransientObject(CASE_ID_JSON_KEY));
            throw exception;
        } catch (Exception exception) {
            log.error("Failed to send Respondent notification for case {}", (String) context.getTransientObject(CASE_ID_JSON_KEY));
            throw new TaskException(exception.getMessage(), exception);
        }

        return caseDataPayload;
    }

    private boolean wasCostsClaimGranted(Map<String, Object> payload) {
        return Optional.ofNullable(payload.get(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD))
            .map(String.class::cast)
            .map(YES_VALUE::equalsIgnoreCase)
            .orElse(false);
    }
}
