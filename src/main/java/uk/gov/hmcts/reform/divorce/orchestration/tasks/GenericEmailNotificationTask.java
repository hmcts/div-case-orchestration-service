package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_TEMPLATE_VARS;

@Component
@Slf4j
@RequiredArgsConstructor
public class GenericEmailNotificationTask implements Task<Map<String, Object>> {

    private final EmailService emailService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> data) {
        String emailAddress = context.getTransientObject(NOTIFICATION_EMAIL);
        EmailTemplateNames template = context.getTransientObject(NOTIFICATION_TEMPLATE);
        Map<String, String> templateVars = context.getTransientObject(NOTIFICATION_TEMPLATE_VARS);
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(data);
        try {
            emailService.sendEmailAndReturnExceptionIfFails(emailAddress, template.name(), templateVars,
                "submission notification", languagePreference);
        } catch (NotificationClientException e) {
            log.warn("Error sending email for case ID: " + context.getTransientObject(CASE_ID_JSON_KEY), e);
            context.setTransientObject(OrchestrationConstants.EMAIL_ERROR_KEY, e.getMessage());
        }

        return data;
    }
}
