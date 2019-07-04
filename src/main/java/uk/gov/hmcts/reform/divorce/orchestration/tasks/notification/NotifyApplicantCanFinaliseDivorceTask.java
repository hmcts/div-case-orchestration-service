package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.TaskCommons;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.CASE_ELIGIBLE_FOR_NOTIFICATION_FOR_APPLICANT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Slf4j
@Component
public class NotifyApplicantCanFinaliseDivorceTask implements Task<Map<String, Object>> {

    private static final String TEMPLATE_ID = "71fd2e7e-42dc-4dcf-a9bb-007ae9d4b27f";
    private static final String EMAIL_REFERENCE = "Applicant can finalise divorce";

    @Autowired
    private TaskCommons taskCommons;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put(NOTIFICATION_CASE_NUMBER_KEY, getMandatoryPropertyValueAsString(payload, D_8_CASE_REFERENCE));
        personalisation.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_FIRST_NAME));
        personalisation.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_LAST_NAME));

        final String caseId = context.getTransientObject(CASE_ID_JSON_KEY);

        try {
            taskCommons.sendEmail(CASE_ELIGIBLE_FOR_NOTIFICATION_FOR_APPLICANT,
                EMAIL_REFERENCE,
                getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_EMAIL),
                personalisation);
            log.info("Eligible for DA - applicant notified. Case id: {}", caseId);
        } catch (TaskException exception) {
            log.error(format("Error sending \"Eligible for DA\" notification to applicant for case id %s", caseId), exception);
            throw exception;
        }

        return payload;
    }

}