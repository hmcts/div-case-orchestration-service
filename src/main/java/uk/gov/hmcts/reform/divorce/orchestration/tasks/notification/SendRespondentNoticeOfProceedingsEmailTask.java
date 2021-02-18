package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EmailConstants.RESPONDENT_SOLICITOR_ORGANISATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_SOLICITOR_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.getRespondentSolicitorOrganisation;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentSolicitorDigital;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendRespondentNoticeOfProceedingsEmailTask implements Task<Map<String, Object>> {

    public static final String EVENT_ISSUE_AOS_FROM_REISSUE = "issueAosFromReissue";
    public static final String EVENT_ISSUE_AOS = "issueAos";

    private static final String EMAIL_DESCRIPTION = "Notice of Proceedings";

    private final EmailService emailService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String caseId = getCaseId(context);

        if (isRespondentSolicitorDigital(caseData)) {
            log.info("CaseId: {}. Respondent Notice Of Proceedings. Sending email to respondent solicitor.", caseId);
            return sendNoticeOfProceedingsToRespondentSolicitor(context, caseData);
        }

        return caseData;
    }

    private Map<String, Object> sendNoticeOfProceedingsToRespondentSolicitor(TaskContext context, Map<String, Object> payload) throws TaskException {
        String solicitorEmail = getMandatoryStringValue(payload, RESPONDENT_SOLICITOR_EMAIL_ADDRESS);
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(payload);

        emailService.sendEmail(
            solicitorEmail,
            EmailTemplateNames.SOL_RESPONDENT_NOTICE_OF_PROCEEDINGS.name(),
            getPersonalisationForSolicitor(context, payload),
            EMAIL_DESCRIPTION + "- Respondent Solicitor",
            languagePreference
        );

        return payload;
    }

    private Map<String, String> getPersonalisationForSolicitor(TaskContext context, Map<String, Object> payload)
        throws TaskException {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(payload),
            NOTIFICATION_RESP_NAME, getRespondentFullName(payload),
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(context),
            NOTIFICATION_SOLICITOR_NAME, getRespondentSolicitorFullName(payload),
            RESPONDENT_SOLICITOR_ORGANISATION, getRespondentSolicitorOrganisation(payload).getOrganisation().getOrganisationName()
        );
    }

    public static boolean isEventSupported(String eventId) {
        return Stream.of(EVENT_ISSUE_AOS, EVENT_ISSUE_AOS_FROM_REISSUE)
            .anyMatch(supportEvent -> supportEvent.equalsIgnoreCase(eventId));
    }
}
