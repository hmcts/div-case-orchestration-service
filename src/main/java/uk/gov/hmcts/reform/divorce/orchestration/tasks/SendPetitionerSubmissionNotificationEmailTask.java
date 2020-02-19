package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RDC_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.formatCaseIdToReferenceNumber;

@Component
public class SendPetitionerSubmissionNotificationEmailTask implements Task<Map<String, Object>> {

    private static final String EMAIL_DESC = "Submission Notification - Petitioner";
    private static final String AMEND_DESC = "Submission Notification For Amend - Petitioner";

    private final EmailService emailService;

    private final TaskCommons taskCommons;

    @Autowired
    public SendPetitionerSubmissionNotificationEmailTask(EmailService emailService, TaskCommons taskCommons) {
        this.emailService = emailService;
        this.taskCommons = taskCommons;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        String petitionerEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);

        if (StringUtils.isNotBlank(petitionerEmail)) {
            Map<String, String> templateVars = new HashMap<>();

            templateVars.put(NOTIFICATION_EMAIL, petitionerEmail);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_FIRST_NAME));
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_LAST_NAME));

            String divorceUnitKey = caseData.get(DIVORCE_UNIT_JSON_KEY).toString();
            Court court = taskCommons.getCourt(divorceUnitKey);
            templateVars.put(NOTIFICATION_RDC_NAME_KEY, court.getIdentifiableCentreName());

            String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
            templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, formatCaseIdToReferenceNumber(caseId));

            Map<String, Object> previousCaseId = (Map<String, Object>) caseData.get(PREVIOUS_CASE_ID_CCD_KEY);
            Optional<LanguagePreference> welshLanguagePreference = CaseDataUtils.getLanguagePreference(caseData.get(LANGUAGE_PREFERENCE_WELSH));

            if (previousCaseId != null) {
                emailService.sendEmail(petitionerEmail, EmailTemplateNames.APPLIC_SUBMISSION_AMEND.name(), templateVars,
                    AMEND_DESC, welshLanguagePreference);
            } else {
                emailService.sendEmail(petitionerEmail, EmailTemplateNames.APPLIC_SUBMISSION.name(), templateVars,
                    EMAIL_DESC, welshLanguagePreference);
            }
        }

        return caseData;
    }

}