package uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice.MarkServedByAlternativeMethodAsYesTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice.MarkServedByProcessServerAsNoTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@AllArgsConstructor
@Slf4j
public class ConfirmServiceByAlternativeMethodWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final MarkServedByProcessServerAsNoTask markServedByProcessServerAsNoTask;
    private final MarkServedByAlternativeMethodAsYesTask markServedByAlternativeMethodAsYesTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} Confirm alternative service workflow is going to be executed.", caseId);

        return this.execute(
            new Task[] {
                markServedByProcessServerAsNoTask,
                markServedByAlternativeMethodAsYesTask
            },
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
