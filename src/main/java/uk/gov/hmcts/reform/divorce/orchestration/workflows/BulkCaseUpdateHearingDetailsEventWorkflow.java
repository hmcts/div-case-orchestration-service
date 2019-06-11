package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDivorceCaseHearingDetailsWithinBulk;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
@AllArgsConstructor
public class BulkCaseUpdateHearingDetailsEventWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ObjectMapper objectMapper;
    private final UpdateDivorceCaseHearingDetailsWithinBulk updateDivorceCaseHearingDetailsWithinBulk;

    public Map<String, Object> run(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException {

        Map<String, Object> bulkCaseDetails = objectMapper.convertValue(callbackRequest.getCaseDetails(), Map.class);

        return this.execute(
                new Task[] {
                    updateDivorceCaseHearingDetailsWithinBulk
                },
                bulkCaseDetails,
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );

    }

}