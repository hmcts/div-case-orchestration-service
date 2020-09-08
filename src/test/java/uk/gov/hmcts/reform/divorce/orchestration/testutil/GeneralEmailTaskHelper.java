package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
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

    public static Map<String, String> getPetitionerTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, "DUMMY")
        );
    }

    public static Map<String, String> getPetitionerSolicitorTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, "DUMMY")
        );
    }

    public static Map<String, String> getRespondentTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, "DUMMY")
        );
    }

    public static Map<String, String> getRespondentSolicitorTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            D8_RESPONDENT_SOLICITOR_NAME, getRespondentSolicitorFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, "DUMMY")
        );
    }

    public static Map<String, String> getCoRespondentTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_CO_RESPONDENT_NAME, getCoRespondentFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, "DUMMY")
        );
    }

    public static Map<String, String> getCoRespondentSolicitorTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            CO_RESPONDENT_SOLICITOR_NAME, getCoRespondentSolicitorFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, "DUMMY")
        );
    }

    public static Map<String, String> getOtherPartyTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            NOTIFICATION_OTHER_NAME, getOtherPartyFullName(caseData),
            NOTIFICATION_GENERAL_EMAIL_DETAILS, getMandatoryPropertyValueAsString(caseData, "DUMMY")
        );

        // TODO: getOtherPartyFullName(caseData) doesnt pull in real data. Need to Update CCD Definitions with something like "OtherPartyName"
    }

    public static TaskContext getTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        return context;
    }
}
