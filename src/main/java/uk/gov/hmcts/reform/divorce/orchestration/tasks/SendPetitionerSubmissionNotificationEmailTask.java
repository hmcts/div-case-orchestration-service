package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RDC_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isPetitionAmended;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.formatCaseIdToReferenceNumber;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendPetitionerSubmissionNotificationEmailTask implements Task<Map<String, Object>> {

    public static final String SUBMITTED_DESC = "Submission Notification - Petitioner";
    public static final String AMEND_DESC = "Submission Notification For Amend - Petitioner";
    public static final String AMEND_SOL_DESC = "Submission Notification For Amend - Solicitor";

    private final EmailService emailService;
    private final TaskCommons taskCommons;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        final String caseId = getCaseId(context);
        log.info("CaseID: {} email to petitioner/solicitor is going to be sent.", caseId);

        if (isPetitionAmended(caseData)) {
            sendApplicationAmendSubmittedEmailToCorrectRecipient(context, caseData);
            log.info("CaseID: {} email sent.", caseId);
        } else {
            if (isPetitionerEmailPopulated(caseData)) {
                sendApplicationSubmittedEmail(context, caseData);
                log.info("CaseID: {} email sent.", caseId);
            } else {
                log.info("CaseID: {} no email sent. There is no petitioner email in this case", caseId);
            }
        }

        return caseData;
    }

    private boolean isPetitionerEmailPopulated(Map<String, Object> caseData) {
        String email = (String) caseData.get(D_8_PETITIONER_EMAIL);
        return StringUtils.isNotEmpty(email);
    }

    private void sendApplicationAmendSubmittedEmailToCorrectRecipient(TaskContext context, Map<String, Object> caseData)
        throws TaskException {
        String caseId = getCaseId(context);

        if (isPetitionerRepresented(caseData)) {
            log.info("CaseID: {} petitioner is represented - email to solicitor.", caseId);
            sendApplicationAmendSubmittedSolicitorEmail(context, caseData);
        } else {
            log.info("CaseID: {} petitioner NOT represented - email to petitioner.", caseId);
            sendApplicationAmendSubmittedEmail(context, caseData);
        }
    }

    private Map<String, String> getPersonalisation(TaskContext context, Map<String, Object> caseData) throws TaskException {
        Map<String, String> personalisation = new HashMap<>();

        personalisation.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_FIRST_NAME));
        personalisation.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_LAST_NAME));

        String divorceUnitKey = caseData.get(DIVORCE_UNIT_JSON_KEY).toString();
        Court court = taskCommons.getCourt(divorceUnitKey);
        personalisation.put(NOTIFICATION_RDC_NAME_KEY, court.getIdentifiableCentreName());

        String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
        personalisation.put(NOTIFICATION_CCD_REFERENCE_KEY, formatCaseIdToReferenceNumber(caseId));

        return personalisation;
    }

    private Map<String, String> getSolicitorPersonalisation(TaskContext context, Map<String, Object> caseData) throws TaskException {
        Map<String, String> personalisation = new HashMap<>();

        String divorceUnitKey = caseData.get(DIVORCE_UNIT_JSON_KEY).toString();
        Court court = taskCommons.getCourt(divorceUnitKey);
        personalisation.put(NOTIFICATION_RDC_NAME_KEY, court.getIdentifiableCentreName());
        personalisation.put(NOTIFICATION_CCD_REFERENCE_KEY, context.getTransientObject(CASE_ID_JSON_KEY));
        personalisation.put(NOTIFICATION_PET_NAME, getPetitionerFullName(caseData));
        personalisation.put(NOTIFICATION_RESP_NAME, getRespondentFullName(caseData));
        personalisation.put(NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(caseData));

        return personalisation;
    }

    private void sendApplicationAmendSubmittedEmail(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String petitionerEmail = getPetitionerEmail(caseData);

        logEvent(context, AMEND_DESC);
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(caseData);

        emailService.sendEmail(
            petitionerEmail,
            EmailTemplateNames.APPLIC_SUBMISSION_AMEND.name(),
            getPersonalisation(context, caseData),
            AMEND_DESC,
            languagePreference
        );

        log.info(
            "CaseID: {} email {} successfully sent to petitioner.",
            getCaseId(context),
            EmailTemplateNames.APPLIC_SUBMISSION_AMEND.name()
        );
    }

    private void sendApplicationAmendSubmittedSolicitorEmail(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String solicitorEmail = getMandatoryStringValue(caseData, PETITIONER_SOLICITOR_EMAIL);

        logEvent(context, AMEND_SOL_DESC);
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(caseData);

        emailService.sendEmail(
            solicitorEmail,
            EmailTemplateNames.APPLIC_SUBMISSION_AMEND_SOLICITOR.name(),
            getSolicitorPersonalisation(context, caseData),
            AMEND_SOL_DESC,
            languagePreference
        );

        log.info(
            "CaseID: {} email {} successfully sent to solicitor.",
            getCaseId(context),
            EmailTemplateNames.APPLIC_SUBMISSION_AMEND_SOLICITOR.name()
        );
    }

    private void sendApplicationSubmittedEmail(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String petitionerEmail = getPetitionerEmail(caseData);

        logEvent(context, SUBMITTED_DESC);
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(caseData);

        emailService.sendEmail(
            petitionerEmail,
            EmailTemplateNames.APPLIC_SUBMISSION.name(),
            getPersonalisation(context, caseData),
            SUBMITTED_DESC,
            languagePreference
        );

        log.info(
            "CaseID: {} email {} successfully sent to petitioner.",
            getCaseId(context),
            EmailTemplateNames.APPLIC_SUBMISSION.name()
        );
    }

    private void logEvent(TaskContext context, String emailDescription) throws TaskException {
        String caseId = getCaseId(context);

        log.info("CaseID: {}, {} is going to be sent.", caseId, emailDescription);
    }

    private String getPetitionerEmail(Map<String, Object> caseData) {
        return getMandatoryStringValue(caseData, D_8_PETITIONER_EMAIL);
    }
}
