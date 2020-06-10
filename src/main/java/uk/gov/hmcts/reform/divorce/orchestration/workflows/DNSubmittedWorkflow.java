package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DecreeNisiAnswersGeneratorTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class DNSubmittedWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final DecreeNisiAnswersGeneratorTask decreeNisiAnswersGenerator;
    private final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @Autowired
    public DNSubmittedWorkflow(DecreeNisiAnswersGeneratorTask decreeNisiAnswersGenerator,
                               AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask) {
        this.decreeNisiAnswersGenerator = decreeNisiAnswersGenerator;
        this.addNewDocumentsToCaseDataTask = addNewDocumentsToCaseDataTask;
    }

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest,
                                   String authToken) throws WorkflowException {

        final List<Task> tasks = new ArrayList<>();

        tasks.add(decreeNisiAnswersGenerator);
        tasks.add(addNewDocumentsToCaseDataTask);

        Task[] taskArr = new Task[tasks.size()];

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        return this.execute(
            tasks.toArray(taskArr),
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

}
