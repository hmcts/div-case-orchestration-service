package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveDnOutcomeCaseFlagTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class RemoveDnOutcomeCaseFlagWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final RemoveDnOutcomeCaseFlagTask removeDnOutcomeCaseFlagTask;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        log.info("CaseID: {} Remove DN outcome case flag Workflow is going to be executed.", caseId);

        return this.execute(
            new Task[] {
                removeDnOutcomeCaseFlagTask
            },
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
