package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataDraftToDivorceFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveNullElements;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveDraft;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetCaseIdAndStateOnSession;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class RetrieveDraftWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final RetrieveDraft retrieveDraft;
    private final CaseDataDraftToDivorceFormatter caseDataToDivorceFormatter;
    private final SetCaseIdAndStateOnSession setCaseIdAndStateOnSession;
    private final RemoveNullElements removeNullElements;

    @Autowired
    public RetrieveDraftWorkflow(RetrieveDraft retrieveDraft,
                                 CaseDataDraftToDivorceFormatter caseDataToDivorceFormatter,
                                 SetCaseIdAndStateOnSession setCaseIdAndStateOnSession,
                                 RemoveNullElements removeNullElements) {
        this.retrieveDraft = retrieveDraft;
        this.caseDataToDivorceFormatter = caseDataToDivorceFormatter;
        this.setCaseIdAndStateOnSession = setCaseIdAndStateOnSession;
        this.removeNullElements = removeNullElements;
    }

    public Map<String, Object> run(String authToken) throws WorkflowException {
        return this.execute(
                new Task[]{
                    retrieveDraft,
                    caseDataToDivorceFormatter,
                    setCaseIdAndStateOnSession,
                    removeNullElements
                },
                new HashMap<>(),
                new ImmutablePair(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }

}
