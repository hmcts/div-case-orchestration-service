package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.TaskCommons;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_NAME_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMAIL_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.DECREE_NISI_CLARIFICATION_SUBMISSION;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Component
@RequiredArgsConstructor
@Slf4j
public class PetitionerClarificationSubmittedNotificationEmailTask implements Task<Map<String, Object>> {

    private static final String EMAIL_DESC = "Clarification Submitted Notification - Petitioner";

    private final EmailService emailService;
    private final TaskCommons taskCommons;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        String petitionerEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);

        String petitionerFirstName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_FIRST_NAME);
        String petitionerLastName = getMandatoryPropertyValueAsString(caseData, D_8_PETITIONER_LAST_NAME);
        String courtName = getMandatoryPropertyValueAsString(caseData, DIVORCE_UNIT_JSON_KEY);
        String caseReference = getMandatoryPropertyValueAsString(caseData, D_8_CASE_REFERENCE);
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(caseData);

        Map<String, String> templateVars = new HashMap<>();
        if (StringUtils.isNotBlank(petitionerEmail)) {
            templateVars.put(COURT_NAME_TEMPLATE_ID, taskCommons.getCourt(courtName).getDivorceCentreName());
            templateVars.put(NOTIFICATION_EMAIL, petitionerEmail);
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, petitionerFirstName);
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, petitionerLastName);
            templateVars.put(NOTIFICATION_REFERENCE_KEY, caseReference);

            try {
                emailService.sendEmailAndReturnExceptionIfFails(petitionerEmail,
                    DECREE_NISI_CLARIFICATION_SUBMISSION.name(), templateVars, EMAIL_DESC, languagePreference);
            } catch (NotificationClientException e) {
                log.warn("Notification fail for case id: {}", caseReference, e);
                context.setTransientObject(EMAIL_ERROR_KEY, e.getMessage());
            }
        } // else solicitor branch not defined yet

        return caseData;
    }
}