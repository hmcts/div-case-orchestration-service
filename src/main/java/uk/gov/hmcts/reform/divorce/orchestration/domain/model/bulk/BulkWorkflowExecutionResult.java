package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Builder
@Data
public class BulkWorkflowExecutionResult {
    private boolean successStatus;
    private Set<String> removableCaseIds;
    private List<Map<String, Object>> failedCases;
}
