package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentAnswersGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
@RequiredArgsConstructor
public class RespondentSubmittedCallbackWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final RespondentAnswersGenerator respondentAnswersGenerator;
    private final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        final List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(respondentAnswersGenerator);
        tasks.add(addNewDocumentsToCaseDataTask);

        Task[] taskArr = new Task[tasks.size()];

        return this.execute(
            tasks.toArray(taskArr),
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }
}
