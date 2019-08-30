package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

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

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Slf4j
@Component
public class NotifyPetitionerForRefusalOrderClarificationTask implements Task<Map<String, Object>> {

    @Autowired
    private EmailService emailService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        Map<String, String> personalisation = new HashMap<>();
        personalisation.put(NOTIFICATION_CASE_NUMBER_KEY, getMandatoryPropertyValueAsString(payload, D_8_CASE_REFERENCE));
        personalisation.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_FIRST_NAME));
        personalisation.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_LAST_NAME));

        final String petitionerEmail = getMandatoryPropertyValueAsString(payload, D_8_PETITIONER_EMAIL);

        emailService.sendEmail(
            petitionerEmail,
            EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION.name(),
            personalisation,
            "Decree Nisi Refusal Order - Clarification"
        );

        return payload;
    }
}