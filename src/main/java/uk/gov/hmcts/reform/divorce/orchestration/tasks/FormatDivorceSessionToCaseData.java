package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.ThreadSafeStatefulTask;

import java.util.Map;

@Component
public class FormatDivorceSessionToCaseData extends ThreadSafeStatefulTask<Map<String, Object>, String> {

    @Autowired
    private CaseFormatterClient caseFormatterClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> sessionData) {
        return caseFormatterClient.transformToCCDFormat(sessionData, getState());
    }
}
