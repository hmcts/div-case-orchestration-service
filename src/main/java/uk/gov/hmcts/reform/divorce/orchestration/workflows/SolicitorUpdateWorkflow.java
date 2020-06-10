package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddMiniPetitionDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@Component
public class SolicitorUpdateWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final AddMiniPetitionDraftTask addMiniPetitionDraftTask;
    private final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @Autowired
    public SolicitorUpdateWorkflow(
            AddMiniPetitionDraftTask addMiniPetitionDraftTask,
            AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask) {
        this.addMiniPetitionDraftTask = addMiniPetitionDraftTask;
        this.addNewDocumentsToCaseDataTask = addNewDocumentsToCaseDataTask;
    }

    public Map<String, Object> run(
            CaseDetails caseDetails,
            final String authToken) throws WorkflowException {

        return this.execute(
                new Task[]{
                    addMiniPetitionDraftTask,
                    addNewDocumentsToCaseDataTask
                }, caseDetails.getCaseData(),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails)
        );
    }
}
