package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.formatCaseIdToReferenceNumber;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;

@Slf4j
@Component
public class SendPetitionerSubmissionNotificationEmailTask implements Task<Map<String, Object>> {

    private static final String SUBMITTED_DESC = "Submission Notification - Petitioner";
    private static final String AMEND_DESC = "Submission Notification For Amend - Petitioner";
    private static final String AMEND_SOL_DESC = "Submission Notification For Amend - Solicitor";

    private final EmailService emailService;

    private final TaskCommons taskCommons;

    @Autowired
    public SendPetitionerSubmissionNotificationEmailTask(EmailService emailService, TaskCommons taskCommons) {
        this.emailService = emailService;
        this.taskCommons = taskCommons;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        if (isPetitionAmended(caseData)) {
            sendApplicationAmendSubmittedEmailToCorrectRecipient(context, caseData);
        } else {
            sendApplicationSubmittedEmail(context, caseData);
        }

        return caseData;
    }

    private void sendApplicationAmendSubmittedEmailToCorrectRecipient(TaskContext context, Map<String, Object> caseData) throws TaskException {
        if (isPetitionerRepresented(caseData)) {
            sendApplicationAmendSubmittedSolicitorEmail(context, caseData);
        } else {
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

        String petitionerFirstName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_FIRST_NAME);
        String petitionerLastName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_LAST_NAME);
        String respondentFirstName = getMandatoryPropertyValueAsString(caseData, RESP_FIRST_NAME_CCD_FIELD);
        String respondentLastName = getMandatoryPropertyValueAsString(caseData, RESP_LAST_NAME_CCD_FIELD);

        String divorceUnitKey = caseData.get(DIVORCE_UNIT_JSON_KEY).toString();
        Court court = taskCommons.getCourt(divorceUnitKey);
        personalisation.put(NOTIFICATION_RDC_NAME_KEY, court.getIdentifiableCentreName());
        personalisation.put(NOTIFICATION_CCD_REFERENCE_KEY, context.getTransientObject(CASE_ID_JSON_KEY));
        personalisation.put(NOTIFICATION_PET_NAME, petitionerFirstName + " " + petitionerLastName);
        personalisation.put(NOTIFICATION_RESP_NAME, respondentFirstName + " " + respondentLastName);

        return personalisation;
    }

    private boolean isPetitionAmended(Map<String, Object> caseData) {
        Map<String, Object> previousCaseId = (Map<String, Object>) caseData.get(PREVIOUS_CASE_ID_CCD_KEY);

        return previousCaseId != null;
    }

    private Map<String, Object> sendApplicationAmendSubmittedEmail(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String petitionerEmail = getMandatoryStringValue(caseData, D_8_PETITIONER_EMAIL);

        logEvent(context, AMEND_DESC);

        emailService.sendEmail(
            petitionerEmail,
            EmailTemplateNames.APPLIC_SUBMISSION_AMEND.name(),
            getPersonalisation(context, caseData),
            AMEND_DESC
        );

        return caseData;
    }

    private Map<String, Object> sendApplicationAmendSubmittedSolicitorEmail(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String solicitorEmail = getMandatoryStringValue(caseData, PETITIONER_SOLICITOR_EMAIL);

        logEvent(context, AMEND_SOL_DESC);

        emailService.sendEmail(
            solicitorEmail,
            EmailTemplateNames.APPLIC_SUBMISSION_AMEND_SOLICITOR.name(),
            getSolicitorPersonalisation(context, caseData),
            AMEND_SOL_DESC
        );

        return caseData;
    }

    private Map<String, Object> sendApplicationSubmittedEmail(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String petitionerEmail = getMandatoryStringValue(caseData, D_8_PETITIONER_EMAIL);

        logEvent(context, SUBMITTED_DESC);

        emailService.sendEmail(
            petitionerEmail,
            EmailTemplateNames.APPLIC_SUBMISSION.name(),
            getPersonalisation(context, caseData),
            SUBMITTED_DESC);

        return caseData;
    }

    private void logEvent(TaskContext context, String emailDescription) throws TaskException {
        String caseId = getCaseId(context);

        log.info(
            "CaseID: {}, {} has been executed.",
            caseId,
            emailDescription
        );
    }
}