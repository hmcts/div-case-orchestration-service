package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;

@Component
@RequiredArgsConstructor
public class RemoveCostOrderDocumentTask implements Task<Map<String, Object>> {

    private final CaseFormatterService caseFormatterService;

    @Override
    public Map<String, Object> execute(TaskContext context, final Map<String, Object> caseData) {
        return caseFormatterService.removeAllDocumentsByType(caseData, COSTS_ORDER_DOCUMENT_TYPE);
    }
}
