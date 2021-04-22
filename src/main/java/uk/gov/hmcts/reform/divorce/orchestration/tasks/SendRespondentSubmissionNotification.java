package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RDC_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.getRespondentEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Slf4j
@RequiredArgsConstructor
public abstract class SendRespondentSubmissionNotification implements Task<Map<String, Object>> {

    protected final CcdUtil ccdUtil;
    private final TaskCommons taskCommons;
    private final TemplateConfigService templateConfigService;

    public abstract String getEmailDescription();

    public abstract EmailTemplateNames getEmailTemplateName();

    protected Map<String, String> getAdditionalTemplateFields(Court court, Map<String, Object> caseDataPayload) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseDataPayload) throws TaskException {
        String caseId = getMandatoryStringValue(caseDataPayload, D_8_CASE_REFERENCE);

        log.info("CaseId: {} Building email template variables for Email: {}", caseId, getEmailDescription());
        Map<String, String> templateFields = getEmailTemplateVariables(caseDataPayload);

        log.info("CaseId: {} Sending email for Email: {}", caseId, getEmailDescription());
        taskCommons.sendEmail(
            getEmailTemplateName(),
            getEmailDescription(),
            getRespondentEmail(caseDataPayload),
            templateFields,
            getLanguagePreference(caseDataPayload));

        return caseDataPayload;
    }

    private Map<String, String> getEmailTemplateVariables(Map<String, Object> caseDataPayload) {
        String petitionerInferredGender = getMandatoryPropertyValueAsString(caseDataPayload, D_8_INFERRED_PETITIONER_GENDER);
        String petRelToRespondent = templateConfigService.getRelationshipTermByGender(petitionerInferredGender, LanguagePreference.ENGLISH);
        String welshPetRelToRespondent = templateConfigService.getRelationshipTermByGender(petitionerInferredGender, LanguagePreference.WELSH);
        String divorceUnitKey = getMandatoryPropertyValueAsString(caseDataPayload, DIVORCE_UNIT_JSON_KEY);
        Court court = taskCommons.getCourt(divorceUnitKey);

        Map<String, String> templateFields = new HashMap<>();
        templateFields.put(NOTIFICATION_CASE_NUMBER_KEY,
            getMandatoryPropertyValueAsString(caseDataPayload, D_8_CASE_REFERENCE));
        templateFields.put(NOTIFICATION_EMAIL,
            getRespondentEmail(caseDataPayload));
        templateFields.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY,
            getMandatoryPropertyValueAsString(caseDataPayload, RESP_FIRST_NAME_CCD_FIELD));
        templateFields.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY,
            getMandatoryPropertyValueAsString(caseDataPayload, RESP_LAST_NAME_CCD_FIELD));
        templateFields.put(NOTIFICATION_HUSBAND_OR_WIFE, petRelToRespondent);
        templateFields.put(NOTIFICATION_RDC_NAME_KEY, court.getIdentifiableCentreName());
        templateFields.put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, welshPetRelToRespondent);

        templateFields.putAll(getAdditionalTemplateFields(court, caseDataPayload));

        return templateFields;
    }

    private LanguagePreference getLanguagePreference(Map<String, Object> caseDataPayload) {
        return CaseDataUtils.getLanguagePreference(caseDataPayload);
    }
}
