package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_REJECT_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_FEES_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_FEE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;

@Slf4j
@AllArgsConstructor
@Component
public class NotifyForRefusalOrderTask implements Task<Map<String, Object>> {

    private static final String EMAIL_DESCRIPTION = "Decree Nisi Refusal Order - ";
    private static final String SOL_PERSONAL_SERVICE_EMAIL = "DN decision made email";

    private final EmailService emailService;
    private final TemplateConfigService templateConfigService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        String caseId = getCaseId(context);

        if (isDnAlreadyGranted(payload)) {
            log.warn("CaseId: {}. DN is already granted. No email will be sent.", caseId);
            return payload;
        }

        log.info("CaseId: {}. DN not granted yet. An email may to be sent.", caseId);

        if (isMoreInfoRequired(payload)) {
            if (isPetitionerRepresented(payload)) {
                log.info("CaseId: {}. DN refused. Sending email to solicitor to provide more info.", caseId);
                return sendDnRefusalToPetitionerSolicitor(context, payload);
            }

            log.info("CaseId: {}. DN refused. Sending email to petitioner to provide more info.", caseId);
            return sendDnRefusalToPetitioner(context, payload);
        } else if (isDnRejected(payload)) {
            if (isPetitionerRepresented(payload)) {
                log.info("CaseId: {}. DN rejected. Petitioner represented. Sending email to solicitor.", caseId);
                return sendDnRejectedToPetitionerSolicitor(context, payload);
            }

            log.info("CaseId: {}. DN rejected. Sending email to petitioner.", caseId);
            return sendDnRejectedToPetitioner(context, payload);
        }

        log.warn("CaseId: {}. Notify for DN refusal - unsupported scenario!", caseId);

        return payload;
    }

    private Map<String, String> getDnRejectPersonalisationForPetitioner(TaskContext context, Map<String, Object> payload) {
        Map<String, String> personalisation = getPersonalisationForPetitioner(payload);

        String petitionerInferredGender = getMandatoryStringValue(payload, D_8_INFERRED_PETITIONER_GENDER);
        String petitionerRelationshipToRespondent = templateConfigService
                .getRelationshipTermByGender(petitionerInferredGender, LanguagePreference.ENGLISH);
        String welshPetRelToRespondent = templateConfigService.getRelationshipTermByGender(petitionerInferredGender,LanguagePreference.WELSH);

        personalisation.put(NOTIFICATION_HUSBAND_OR_WIFE, petitionerRelationshipToRespondent);
        personalisation.put(NOTIFICATION_FEES_KEY, getFormattedFeeAmount(context));
        personalisation.put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, welshPetRelToRespondent);

        return personalisation;
    }

    private String getFormattedFeeAmount(TaskContext context) {
        return ((FeeResponse) context.getTransientObject(PETITION_FEE_JSON_KEY)).getFormattedFeeAmount();
    }

    private Map<String, String> getPersonalisationForPetitioner(Map<String, Object> payload) {
        Map<String, String> personalisation = new HashMap<>();

        personalisation.put(NOTIFICATION_CASE_NUMBER_KEY, getMandatoryStringValue(payload, D_8_CASE_REFERENCE));
        personalisation.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, getMandatoryStringValue(payload, D_8_PETITIONER_FIRST_NAME));
        personalisation.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, getMandatoryStringValue(payload, D_8_PETITIONER_LAST_NAME));

        return personalisation;
    }

    private Map<String, String> getPersonalisationForSolicitor(TaskContext context, Map<String, Object> payload)
        throws TaskException {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(payload),
            NOTIFICATION_RESP_NAME, getRespondentFullName(payload),
            NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(payload),
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(context),
            NOTIFICATION_FEES_KEY, getFormattedFeeAmount(context)
        );
    }

    private Map<String, Object> sendDnRefusalToPetitioner(TaskContext context, Map<String, Object> payload) throws TaskException {
        return sendEmailToPetitioner(
            context,
            payload,
            EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION,
            getPersonalisationForPetitioner(payload),
            "Clarification"
        );
    }

    private Map<String, Object> sendDnRejectedToPetitioner(TaskContext context, Map<String, Object> payload) throws TaskException {
        return sendEmailToPetitioner(
            context,
            payload,
            EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_REJECTION,
            getDnRejectPersonalisationForPetitioner(context, payload),
            "Rejection"
        );
    }

    private Map<String, Object> sendEmailToPetitioner(
        TaskContext context,
        Map<String, Object> payload,
        EmailTemplateNames templateId,
        Map<String, String> personalisation,
        String description) throws TaskException {

        String caseId = getCaseId(context);
        String petitionerEmail = getOptionalPropertyValueAsString(payload, D_8_PETITIONER_EMAIL, null);

        if (petitionerEmail == null) {
            log.debug("CaseId {}: There is no petitioner email, It's solicitor journey", caseId);
            return payload;
        }
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(payload);
        emailService.sendEmail(petitionerEmail, templateId.name(), personalisation, EMAIL_DESCRIPTION + description, languagePreference);

        log.info("CaseId {}: Email to petitioner sent.", caseId);

        return payload;
    }

    private Map<String, Object> sendDnRejectedToPetitionerSolicitor(TaskContext context, Map<String, Object> payload) throws TaskException {
        String solicitorEmail = getMandatoryStringValue(payload, PETITIONER_SOLICITOR_EMAIL);
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(payload);

        emailService.sendEmail(
            solicitorEmail,
            EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_REJECTION_SOLICITOR.name(),
            getPersonalisationForSolicitor(context, payload),
            EMAIL_DESCRIPTION + "Rejection",
            languagePreference
        );

        return payload;
    }

    private Map<String, Object> sendDnRefusalToPetitionerSolicitor(TaskContext context, Map<String, Object> payload)
        throws TaskException {
        String solicitorEmail = getMandatoryStringValue(payload, PETITIONER_SOLICITOR_EMAIL);
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(payload);

        emailService.sendEmail(
            solicitorEmail,
            EmailTemplateNames.SOL_DN_DECISION_MADE.name(),
            getPersonalisationForSolicitor(context, payload),
            SOL_PERSONAL_SERVICE_EMAIL,
            languagePreference
        );

        return payload;
    }

    private boolean isDnRejected(Map<String, Object> payload) {
        return DN_REFUSED_REJECT_OPTION.equalsIgnoreCase((String) payload.get(REFUSAL_DECISION_CCD_FIELD));
    }

    private boolean isMoreInfoRequired(Map<String, Object> payload) {
        return REFUSAL_DECISION_MORE_INFO_VALUE.equalsIgnoreCase((String) payload.get(REFUSAL_DECISION_CCD_FIELD));
    }

    private boolean isDnAlreadyGranted(Map<String, Object> payload) {
        return YES_VALUE.equalsIgnoreCase((String) payload.get(DECREE_NISI_GRANTED_CCD_FIELD));
    }
}
