package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

@Component
public class FormatDivorceSessionToAosCaseData implements Task<Map<String, Object>> {

    private final CaseFormatterClient caseFormatterClient;

    @Autowired
    public FormatDivorceSessionToAosCaseData(CaseFormatterClient caseFormatterClient) {
        this.caseFormatterClient = caseFormatterClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> sessionData) {
        return caseFormatterClient.transformToAosCaseFormat(sessionData);
    }
}
