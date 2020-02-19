package uk.gov.hmcts.reform.divorce.orchestration.tasks;

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
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendSolicitorPersonalServiceEmailTask extends SolicitorEmailTask implements Task<Map<String, Object>> {

    private static final String SOL_PERSONAL_SERVICE_EMAIL = "Solicitor Personal Service email";

    private final EmailService emailService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String petSolicitorEmail = getMandatoryPropertyValueAsString(caseData, PET_SOL_EMAIL);
        String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
        Optional<LanguagePreference> welshLanguagePreference = CaseDataUtils.getLanguagePreference(caseData.get(LANGUAGE_PREFERENCE_WELSH));
        Map<String, String> templateVars = buildEmailTemplateVars(petSolicitorEmail, caseId, caseData);

        try {
            emailService.sendEmailAndReturnExceptionIfFails(
                    petSolicitorEmail,
                    EmailTemplateNames.SOL_PERSONAL_SERVICE.name(),
                    templateVars,
                    SOL_PERSONAL_SERVICE_EMAIL,
                    welshLanguagePreference
            );
            return caseData;
        } catch (NotificationClientException e) {
            log.error(
                    String.format("Error sending solicitor personal service notification for case %s", caseId),
                    e
            );
            throw new TaskException(e);
        }
    }
}
