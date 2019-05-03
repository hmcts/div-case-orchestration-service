package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseWithId;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class GetCaseWithIdWorkflow extends DefaultWorkflow<CaseDetails> {
    private final GetCaseWithId getCaseWithId;

    @Autowired
    public GetCaseWithIdWorkflow(GetCaseWithId getCaseWithId) {
        this.getCaseWithId = getCaseWithId;
    }

    public CaseDetails run(String caseId) throws WorkflowException {
        this.execute(
            new Task[] {
                getCaseWithId
            },
            null,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );

        return (CaseDetails) this.getContext().getTransientObject(CASE_DETAILS_JSON_KEY);
    }
}