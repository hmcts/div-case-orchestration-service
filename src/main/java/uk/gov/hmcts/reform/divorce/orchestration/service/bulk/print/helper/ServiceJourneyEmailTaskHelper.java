package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EmailVars.EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EmailVars.SOLICITOR_ORGANISATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.getRespondentSolicitorEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.getRespondentSolicitorOrganisation;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceJourneyEmailTaskHelper {

    public static Map<String, String> citizenTemplateVariables(Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData)
        );
    }

    public static Map<String, String> defaultSolicitorTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(caseData)
        );
    }

    public static Map<String, String> respondentSolicitorTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext),
            NOTIFICATION_SOLICITOR_NAME, getRespondentSolicitorFullName(caseData)
        );
    }

    public static Map<String, String> respondentSolicitorWithOrgTemplateVariables(TaskContext taskContext, Map<String, Object> caseData) {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put(NOTIFICATION_PET_NAME, getPetitionerFullName(caseData));
        templateVariables.put(NOTIFICATION_RESP_NAME, getRespondentFullName(caseData));
        templateVariables.put(NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext));
        templateVariables.put(NOTIFICATION_SOLICITOR_NAME, getRespondentSolicitorFullName(caseData));
        templateVariables.put(EMAIL_ADDRESS, getRespondentSolicitorEmail(caseData));
        templateVariables.put(SOLICITOR_ORGANISATION, getRespondentSolicitorOrganisation(caseData)
            .getOrganisation().getOrganisationName());

        return templateVariables;
    }
}
