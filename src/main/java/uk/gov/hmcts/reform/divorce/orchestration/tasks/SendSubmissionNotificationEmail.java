package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;

@Component
@Slf4j
public class SendSubmissionNotificationEmail implements Task<Map<String, Object>> {

    private final EmailService emailService;

    @Autowired
    public SendSubmissionNotificationEmail(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        String petitionerEmail = (String) caseData.get(D_8_PETITIONER_EMAIL);

        if (StringUtils.isNotBlank(petitionerEmail)) {
            Map<String, String> templateVars = new HashMap<>();

            templateVars.put("email address", petitionerEmail);
            templateVars.put("first name", (String) caseData.get(D_8_PETITIONER_FIRST_NAME));
            templateVars.put("last name", (String) caseData.get(D_8_PETITIONER_LAST_NAME));
            templateVars.put("RDC name", CourtEnum.valueOf(
                caseData.get(DIVORCE_UNIT_JSON_KEY).toString().toUpperCase(Locale.UK)).getDisplayName()
            );
            templateVars.put("CCD reference", formatReferenceId((String) context.getTransientObject(CASE_ID_JSON_KEY)));

            emailService.sendSubmissionNotificationEmail(petitionerEmail, templateVars);
        }

        return caseData;
    }

    private String formatReferenceId(String referenceId) {
        try {
            return String.format("%s-%s-%s-%s",
                    referenceId.substring(0, 4),
                    referenceId.substring(4, 8),
                    referenceId.substring(8, 12),
                    referenceId.substring(12));
        } catch (Exception exception) {
            log.warn("Error formatting case reference {}", referenceId);
            return referenceId;
        }
    }
}
