package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;

@Slf4j
@Component
public class SendNoticeOfProceedingsEmailTask implements Task<Map<String, Object>> {

    public static final String EVENT_ISSUE_AOS_FROM_REISSUE = "issueAosFromReissue";
    public static final String EVENT_ISSUE_AOS = "issueAos";

    private static final String EMAIL_DESCRIPTION = "Notice of Proceedings";

    @Autowired
    EmailService emailService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String caseId = getCaseId(context);

        if (!isEventSupported(context)) {
            log.info("CaseId: {}. Notice of proceeding will not be sent becasue this is wrong event.", caseId);
            return caseData;
        }

        if (isPetitionerRepresented(caseData)) {
            log.info("CaseId: {}. Notice Of Proceedings. Sending email to solicitor.", caseId);
            return sendNoticeOfProceedingsToPetitionerSolicitor(context, caseData);
        }

        log.info("CaseId: {}. Notice Of Proceedings. Sending email to petitioner.", caseId);
        return sendNoticeOfProceedingsToPetitioner(caseData);
    }

    private boolean isEventSupported(TaskContext context) {
        String eventId = context.getTransientObject(CASE_EVENT_ID_JSON_KEY);

        return Stream.of(EVENT_ISSUE_AOS, EVENT_ISSUE_AOS_FROM_REISSUE)
            .anyMatch(supportEvent -> supportEvent.equalsIgnoreCase(eventId));
    }

    private Map<String, Object> sendNoticeOfProceedingsToPetitionerSolicitor(TaskContext context, Map<String, Object> payload) throws TaskException {
        String solicitorEmail = getMandatoryStringValue(payload, PETITIONER_SOLICITOR_EMAIL);

        emailService.sendEmail(
            solicitorEmail,
            EmailTemplateNames.SOL_PETITIONER_NOTICE_OF_PROCEEDINGS.name(),
            getPersonalisationForSolicitor(context, payload),
            EMAIL_DESCRIPTION + "- Solicitor"
        );

        return payload;
    }

    private Map<String, Object> sendNoticeOfProceedingsToPetitioner(Map<String, Object> payload) {
        String petitionerEmail = getMandatoryStringValue(payload, D_8_PETITIONER_EMAIL);

        emailService.sendEmail(
            petitionerEmail,
            EmailTemplateNames.PETITIONER_NOTICE_OF_PROCEEDINGS.name(),
            getPersonalisationForPetitioner(payload),
            EMAIL_DESCRIPTION
        );

        return payload;
    }

    private Map<String, String> getPersonalisationForSolicitor(TaskContext context, Map<String, Object> payload)
        throws TaskException {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, getPetitionerFullName(payload),
            NOTIFICATION_RESP_NAME, getRespondentFullName(payload),
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(context),
            NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(payload)
        );
    }

    private Map<String, String> getPersonalisationForPetitioner(Map<String, Object> payload) {
        Map<String, String> personalisation = new HashMap<>();

        personalisation.put(NOTIFICATION_CASE_NUMBER_KEY, getMandatoryStringValue(payload, D_8_CASE_REFERENCE));
        personalisation.put(NOTIFICATION_PET_NAME, getPetitionerFullName(payload));
        personalisation.put(NOTIFICATION_RESP_NAME, getRespondentFullName(payload));

        return personalisation;
    }
}
