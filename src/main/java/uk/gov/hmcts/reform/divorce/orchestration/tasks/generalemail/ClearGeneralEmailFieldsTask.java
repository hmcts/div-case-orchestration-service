package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_OTHER_RECIPIENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_PARTIES;

@Component
public class ClearGeneralEmailFieldsTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        Map<String, Object> payloadToReturn = new HashMap<>(payload);

        payloadToReturn.remove(GENERAL_EMAIL_PARTIES);
        payloadToReturn.remove(GENERAL_EMAIL_DETAILS);
        payloadToReturn.remove(GENERAL_EMAIL_OTHER_RECIPIENT_NAME);
        payloadToReturn.remove(GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL);

        return payloadToReturn;
    }
}
