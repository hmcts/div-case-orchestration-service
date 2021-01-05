package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CoRespondentAnswersGeneratorTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class GenerateCoRespondentAnswersWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final CoRespondentAnswersGeneratorTask coRespondentAnswersGeneratorTask;
    private final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @Autowired
    public GenerateCoRespondentAnswersWorkflow(CoRespondentAnswersGeneratorTask coRespondentAnswersGeneratorTask,
                                               AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask) {
        this.coRespondentAnswersGeneratorTask = coRespondentAnswersGeneratorTask;
        this.addNewDocumentsToCaseDataTask = addNewDocumentsToCaseDataTask;
    }

    public Map<String, Object> run(CaseDetails caseDetails, String authorizationToken) throws WorkflowException {
        return execute(
                new Task[] {
                    coRespondentAnswersGeneratorTask,
                    addNewDocumentsToCaseDataTask
                },
                caseDetails.getCaseData(),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authorizationToken),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId())
        );
    }
}
