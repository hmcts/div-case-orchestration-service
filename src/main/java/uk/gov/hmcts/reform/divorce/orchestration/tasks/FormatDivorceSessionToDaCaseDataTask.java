package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FormatDivorceSessionToDaCaseDataTask implements Task<Map<String, Object>> {

    private final CaseFormatterClient caseFormatterClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> sessionData) {
        return caseFormatterClient.transformToDaCaseFormat(
            sessionData
        );
    }
}
