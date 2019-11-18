package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PET_SOL_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendDnDecisionSolNotificationTask extends SolicitorEmailTask implements Task<Map<String, Object>> {

    private static final String SOL_PERSONAL_SERVICE_EMAIL = "DN decision made email";

    private final EmailService emailService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        Optional<String> solicitorEmail = Optional.ofNullable(getOptionalPropertyValueAsString(caseData, PET_SOL_EMAIL, null));
        if (!isDnGranted(caseData) && solicitorEmail.isPresent()) {
            String petSolicitorEmail = getMandatoryPropertyValueAsString(caseData, PET_SOL_EMAIL);
            String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
            Map<String, String> templateVars = buildEmailTemplateVars(petSolicitorEmail, caseId, caseData);

            try {
                emailService.sendEmailAndReturnExceptionIfFails(
                        petSolicitorEmail,
                        EmailTemplateNames.SOL_DN_DECISION_MADE.name(),
                        templateVars,
                        SOL_PERSONAL_SERVICE_EMAIL
                );
                return caseData;
            } catch (NotificationClientException e) {
                log.error(
                        String.format("Error sending DN solicitor notification for case %s", caseId),
                        e
                );
                throw new TaskException(e);
            }
        }
        return caseData;
    }

    private boolean isDnGranted(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(DECREE_NISI_GRANTED_CCD_FIELD));
    }
}
