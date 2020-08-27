package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

public class ServiceJourneyEmailTaskHelper {

    public static Map<String, String> getSolicitorTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(caseData)
        );
    }

    public static Map<String, String> getCitizenTemplateVariables(Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData)
        );
    }

    public static void removeAllEmailAddresses(Map<String, Object> caseData) {
        caseData.remove(PETITIONER_EMAIL);
        caseData.remove(PETITIONER_SOLICITOR_EMAIL);
        caseData.remove(CO_RESP_EMAIL_ADDRESS);
        caseData.remove(RESPONDENT_EMAIL_ADDRESS);
    }

    public static Map<String, String> getExpectedNotificationTemplateVars(
        boolean isPetitionerRepresented, TaskContext taskContext, Map<String, Object> caseData) {
        return isPetitionerRepresented
            ? getSolicitorTemplateVariables(taskContext, caseData)
            : getCitizenTemplateVariables(caseData);
    }
}
