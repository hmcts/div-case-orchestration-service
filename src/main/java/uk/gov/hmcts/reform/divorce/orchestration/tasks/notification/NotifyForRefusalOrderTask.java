package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

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
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_FEE_JSON_KEY;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_FEES_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Slf4j
@AllArgsConstructor
@Component
public class NotifyForRefusalOrderTask implements Task<Map<String, Object>> {

    private EmailService emailService;
    private TemplateConfigService templateConfigService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        if (NO_VALUE.equalsIgnoreCase((String) payload.get(DECREE_NISI_GRANTED_CCD_FIELD))) {
            Map<String, String> personalisation = new HashMap<>();

            personalisation.put(NOTIFICATION_CASE_NUMBER_KEY, getMandatoryPropertyValueAsString(payload, D_8_CASE_REFERENCE));
            personalisation.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_FIRST_NAME));
            personalisation.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_LAST_NAME));

            final String petitionerEmail = getOptionalPropertyValueAsString(payload, D_8_PETITIONER_EMAIL,  null);
            if  (petitionerEmail == null) {
                log.debug("There is no petitioner email, It's solicitor journey");
                return payload;
            }
            String refusalReason = (String) payload.get(REFUSAL_DECISION_CCD_FIELD);
            Optional<LanguagePreference> languagePreference = CaseDataUtils.getLanguagePreference(payload);
            if (REFUSAL_DECISION_MORE_INFO_VALUE.equalsIgnoreCase(refusalReason)) {
                emailService.sendEmail(
                    petitionerEmail,
                    EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION.name(),
                    personalisation,
                    "Decree Nisi Refusal Order - Clarification",
                    languagePreference
                );
            } else if (DN_REFUSED_REJECT_OPTION.equalsIgnoreCase(refusalReason)) {
                FeeResponse amendFee = context.getTransientObject(AMEND_PETITION_FEE_JSON_KEY);
                String petitionerInferredGender = getMandatoryPropertyValueAsString(payload,
                    D_8_INFERRED_PETITIONER_GENDER);
                String petRelToRespondent = templateConfigService.getRelationshipTermByGender(petitionerInferredGender, LanguagePreference.ENGLISH);
                String welshPetRelToRespondent = templateConfigService.getRelationshipTermByGender(petitionerInferredGender,LanguagePreference.WELSH);
                personalisation.put(NOTIFICATION_HUSBAND_OR_WIFE, petRelToRespondent);
                personalisation.put(NOTIFICATION_FEES_KEY, amendFee.getFormattedFeeAmount());
                personalisation.put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, welshPetRelToRespondent);

                emailService.sendEmail(
                    petitionerEmail,
                    EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_REJECTION.name(),
                    personalisation,
                    "Decree Nisi Refusal Order - Rejection",
                    languagePreference
                );
            }
        }

        return payload;
    }
}