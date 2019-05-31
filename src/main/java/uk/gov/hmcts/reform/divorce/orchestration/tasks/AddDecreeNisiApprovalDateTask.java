package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_APPROVAL_DATE_FIELD;

@Component
public class AddDecreeNisiApprovalDateTask implements Task<Map<String, Object>> {

    private CcdUtil ccdUtil;

    public AddDecreeNisiApprovalDateTask(@Autowired CcdUtil ccdUtil) {
        this.ccdUtil = ccdUtil;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> incomingPayload) {
        Map<String, Object> payload = new HashMap<>(incomingPayload);

        payload.put(DN_APPROVAL_DATE_FIELD, ccdUtil.getCurrentDateCcdFormat());

        return payload;
    }

}