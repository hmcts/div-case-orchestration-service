package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CO_RESPONDENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OTHER_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
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
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

public class GeneralEmailTaskHelper {

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
            case OTHER:
                return getOtherPartyTemplateVariables(taskContext, caseData);
            default:
                throw new IllegalArgumentException("Notification template variable party was not set.");
        }
    }

    private static Map<String, String> getPetitionerTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, GENERAL_EMAIL_DETAILS)
        );
    }

    private static Map<String, String> getPetitionerSolicitorTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, GENERAL_EMAIL_DETAILS)
        );
    }

    private static Map<String, String> getRespondentTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, GENERAL_EMAIL_DETAILS)
        );
    }

    private static Map<String, String> getRespondentSolicitorTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            D8_RESPONDENT_SOLICITOR_NAME, getRespondentSolicitorFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, GENERAL_EMAIL_DETAILS)
        );
    }

    private static Map<String, String> getCoRespondentTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_CO_RESPONDENT_NAME, getCoRespondentFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, GENERAL_EMAIL_DETAILS)
        );
    }

    private static Map<String, String> getCoRespondentSolicitorTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            CO_RESPONDENT_SOLICITOR_NAME, getCoRespondentSolicitorFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, GENERAL_EMAIL_DETAILS)
        );
    }

    private static Map<String, String> getOtherPartyTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            NOTIFICATION_OTHER_NAME, getOtherPartyFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, GENERAL_EMAIL_DETAILS)
        );

        // TODO: getOtherPartyFullName(caseData) doesnt pull in real data. Need to Update CCD Definitions with something like "OtherPartyName"
    }

    public static String getRepresentedSubject(TaskContext context, Map<String, Object> caseData) {
        return format(
            "%s vs %s: Divorce case number %s",
            getPetitionerFullName(caseData),
            getRespondentFullName(caseData),
            getCaseId(context)
        );
    }

    public static String getNotRepresentedSubject(TaskContext context) {
        return "Divorce case number " + getCaseId(context);
    }

    public enum Party {
        PETITIONER,
        PETITIONER_SOLICITOR,
        RESPONDENT,
        RESPONDENT_SOLICITOR,
        CO_RESPONDENT,
        CO_RESPONDENT_SOLICITOR,
        OTHER
    }

}
