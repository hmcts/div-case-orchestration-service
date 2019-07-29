package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.formatCaseIdToReferenceNumber;

@Slf4j
@Component
public class SendRespondentSolicitorAosInvitationEmail implements Task<Map<String, Object>> {

    private static final String ACCESS_CODE = "access code";
    private static final String SOLICITORS_NAME = "solicitors name";
    private static final String RESPONDENT_SOLICITOR_S_AOS_INVITATION = "Respondent solicitor's AOS invitation";
    private static final String sirMadam = "Sir/Madam";

    private final TaskCommons taskCommons;

    @Autowired
    public SendRespondentSolicitorAosInvitationEmail(TaskCommons taskCommons) {
        this.taskCommons = taskCommons;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {

        Map<String, String> templateVars = new HashMap<>();

        templateVars.put(NOTIFICATION_CCD_REFERENCE_KEY, (String) payload.get(D_8_CASE_REFERENCE));
        String solicitorName = (String) payload.get(D8_RESPONDENT_SOLICITOR_NAME);
        if (Strings.isNullOrEmpty(solicitorName)) {
            solicitorName = sirMadam;
        }
        templateVars.put(SOLICITORS_NAME, solicitorName);
        templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, (String) payload.get(RESP_FIRST_NAME_CCD_FIELD));
        templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, (String) payload.get(RESP_LAST_NAME_CCD_FIELD));
        templateVars.put(ACCESS_CODE, context.getTransientObject(RESPONDENT_PIN));
        String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
        templateVars.put(NOTIFICATION_CASE_NUMBER_KEY, formatCaseIdToReferenceNumber(caseId));

        try {
            taskCommons.sendEmail(
                    EmailTemplateNames.RESPONDENT_SOLICITOR_AOS_INVITATION,
                    RESPONDENT_SOLICITOR_S_AOS_INVITATION,
                    (String) payload.get(D8_RESPONDENT_SOLICITOR_EMAIL),
                    templateVars
            );
        } catch (TaskException e) {
            context.setTaskFailed(true);
            log.error("Error sending AOS invitation letter to solicitor for case ID: {}", caseId);
            throw e;
        }

        return payload;
    }
}
