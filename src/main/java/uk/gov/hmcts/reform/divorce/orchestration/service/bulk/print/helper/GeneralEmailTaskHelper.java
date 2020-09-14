package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralEmailCaseDataExtractor;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CO_RESPONDENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OTHER_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getCoRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getCoRespondentSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getOtherPartyFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

public class GeneralEmailTaskHelper {

    public enum Party {
        PETITIONER,
        PETITIONER_SOLICITOR,
        RESPONDENT,
        RESPONDENT_SOLICITOR,
        CO_RESPONDENT,
        CO_RESPONDENT_SOLICITOR,
        OTHER
    }

    public static Map<String, String> getExpectedNotificationTemplateVars(
        Party party, TaskContext taskContext, Map<String, Object> caseData) {
        switch (party) {
            case PETITIONER:
                return getPetitionerTemplateVariables(taskContext, caseData);
            case PETITIONER_SOLICITOR:
                return getPetitionerSolicitorTemplateVariables(taskContext, caseData);
            case RESPONDENT:
                return getRespondentTemplateVariables(taskContext, caseData);
            case RESPONDENT_SOLICITOR:
                return getRespondentSolicitorTemplateVariables(taskContext, caseData);
            case CO_RESPONDENT:
                return getCoRespondentTemplateVariables(taskContext, caseData);
            case CO_RESPONDENT_SOLICITOR:
                return getCoRespondentSolicitorTemplateVariables(taskContext, caseData);
            default:
                return getOtherPartyTemplateVariables(taskContext, caseData);
        }
    }

    private static Map<String, String> getDefaultTemplateVars(TaskContext taskContext, Map<String, Object> caseData) {
        return new HashMap<>(ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, GeneralEmailCaseDataExtractor.getGeneralEmailDetails(caseData)
        )
        );
    }

    private static Map<String, String> getPetitionerTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        Map<String, String> templateVars = getDefaultTemplateVars(taskContext, caseData);
        templateVars.put(NOTIFICATION_PET_NAME, getPetitionerFullName(caseData));

        return templateVars;
    }

    private static Map<String, String> getPetitionerSolicitorTemplateVariables(TaskContext context, Map<String, Object> caseData) {
        Map<String, String> templateVars = getDefaultTemplateVars(context, caseData);
        templateVars.put(NOTIFICATION_PET_NAME, getPetitionerFullName(caseData));
        templateVars.put(NOTIFICATION_RESP_NAME, getRespondentFullName(caseData));
        templateVars.put(NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(caseData));

        return templateVars;
    }

    private static Map<String, String> getRespondentTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        Map<String, String> templateVars = getDefaultTemplateVars(taskContext, caseData);
        templateVars.put(NOTIFICATION_RESP_NAME, getRespondentFullName(caseData));

        return templateVars;
    }

    private static Map<String, String> getRespondentSolicitorTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        Map<String, String> templateVars = getDefaultTemplateVars(taskContext, caseData);
        templateVars.put(NOTIFICATION_PET_NAME, getPetitionerFullName(caseData));
        templateVars.put(NOTIFICATION_RESP_NAME, getRespondentFullName(caseData));
        templateVars.put(NOTIFICATION_RESPONDENT_SOLICITOR_NAME, getRespondentSolicitorFullName(caseData));

        return templateVars;
    }

    private static Map<String, String> getCoRespondentTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        Map<String, String> templateVars = getDefaultTemplateVars(taskContext, caseData);
        templateVars.put(NOTIFICATION_CO_RESPONDENT_NAME, getCoRespondentFullName(caseData));

        return templateVars;
    }

    private static Map<String, String> getCoRespondentSolicitorTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        Map<String, String> templateVars = getDefaultTemplateVars(taskContext, caseData);
        templateVars.put(NOTIFICATION_PET_NAME, getPetitionerFullName(caseData));
        templateVars.put(NOTIFICATION_RESP_NAME, getRespondentFullName(caseData));
        templateVars.put(NOTIFICATION_CO_RESPONDENT_SOLICITOR_NAME, getCoRespondentSolicitorFullName(caseData));

        return templateVars;
    }

    private static Map<String, String> getOtherPartyTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        Map<String, String> templateVars = getDefaultTemplateVars(taskContext, caseData);
        templateVars.put(NOTIFICATION_PET_NAME, getPetitionerFullName(caseData));
        templateVars.put(NOTIFICATION_RESP_NAME, getRespondentFullName(caseData));
        templateVars.put(NOTIFICATION_OTHER_NAME, getOtherPartyFullName(caseData));

        return templateVars;
    }

    public static String getRepresentedSubject(TaskContext taskContext, Map<String, Object> caseData) {
        return format(
            "%s vs %s: Divorce case number %s",
            getPetitionerFullName(caseData),
            getRespondentFullName(caseData),
            getCaseId(taskContext)
        );
    }

    public static String getNotRepresentedSubject(TaskContext context) {
        return "Divorce case number " + getCaseId(context);
    }
}