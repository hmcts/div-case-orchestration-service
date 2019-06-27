package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

@Component
public class RemoveMiniPetitionDraftDocumentsTask implements Task<Map<String, Object>> {

    private final CaseFormatterClient caseFormatterClient;

    @Autowired
    public RemoveMiniPetitionDraftDocumentsTask(final CaseFormatterClient caseFormatterClient) {
        this.caseFormatterClient = caseFormatterClient;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) {
        return caseFormatterClient.removeAllPetitionDocuments(caseData);
    }
}
