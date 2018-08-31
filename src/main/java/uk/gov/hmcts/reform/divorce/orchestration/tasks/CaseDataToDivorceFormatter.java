package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

@Component
public class CaseDataToDivorceFormatter implements Task<Map<String, Object>> {
    private final CaseFormatterClient caseFormatterClient;

    @Autowired
    public CaseDataToDivorceFormatter(CaseFormatterClient caseFormatterClient) {
        this.caseFormatterClient = caseFormatterClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context,
                                       Map<String, Object> payload,
                                       Object... params) throws TaskException {
        return caseFormatterClient.transformToDivorceFormat(payload,
                String.valueOf(params[0]));
    }
}
