package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.service.DataMapTransformer;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FormatDivorceSessionToCaseDataTask implements Task<Map<String, Object>> {

    private final DataMapTransformer dataMapTransformer;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        return dataMapTransformer.transformDivorceCaseDataToCourtCaseData(payload);
    }

}