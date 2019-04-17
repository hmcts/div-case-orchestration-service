package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
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

@Component
@Slf4j
public class SendPetitionerClarificationRequestEmail implements Task<Map<String, Object>> {

    private final EmailService emailService;

    @Autowired
    public SendPetitionerClarificationRequestEmail(final EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) {

        final String petitionerEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);
        if (StringUtils.isNotBlank(petitionerEmail)) {
            final Map<String, String> templateVars = new HashMap<>();

            templateVars.put(NOTIFICATION_CASE_NUMBER_KEY, (String) caseData.get(D_8_CASE_REFERENCE));
            templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_FIRST_NAME));
            templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, (String) caseData.get(D_8_PETITIONER_LAST_NAME));
            emailService.sendPetitionerClarificationRequestEmail(petitionerEmail, templateVars);
        } else {
            log.warn("petitioner email address found to be empty for case {}", caseData.get(D_8_CASE_REFERENCE));
        }
        return caseData;
    }
}
