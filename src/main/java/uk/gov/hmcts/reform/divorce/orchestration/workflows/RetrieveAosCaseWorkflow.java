package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataToDivorceFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CmsCaseRetriever;

import java.util.HashMap;
import java.util.Map;

@Component
public class RetrieveAosCaseWorkflow extends DefaultWorkflow<Map<String, Object>> {
    private final CmsCaseRetriever cmsCaseRetriever;
    private final CaseDataToDivorceFormatter caseDataToDivorceFormatter;

    @Autowired
    public RetrieveAosCaseWorkflow(CmsCaseRetriever cmsCaseRetriever,
                                   CaseDataToDivorceFormatter caseDataToDivorceFormatter) {
        this.cmsCaseRetriever = cmsCaseRetriever;
        this.caseDataToDivorceFormatter = caseDataToDivorceFormatter;
    }

    public Map<String, Object> run(boolean chechCcd,
                                   String authToken) throws WorkflowException {
        Map<String, Object> payload = new HashMap<>();
        return this.execute(
                new Task[]{
                    cmsCaseRetriever,
                    caseDataToDivorceFormatter
                },
                payload,
                authToken,
                chechCcd);
    }
}
