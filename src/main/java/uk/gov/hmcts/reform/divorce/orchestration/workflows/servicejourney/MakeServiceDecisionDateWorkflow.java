package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.MakeServiceDecisionDateTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class MakeServiceDecisionDateWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final MakeServiceDecisionDateTask makeServiceDecisionDateTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {

        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} Service Application Decision Date workflow is going to be executed.", caseId);

        return this.execute(
            new Task[] {
                makeServiceDecisionDateTask
            },
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
