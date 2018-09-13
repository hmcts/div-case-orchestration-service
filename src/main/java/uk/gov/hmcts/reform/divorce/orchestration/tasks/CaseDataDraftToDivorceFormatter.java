package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_DRAFT_KEY;

@Component
public class CaseDataDraftToDivorceFormatter implements Task<Map<String, Object>> {
    private final CaseFormatterClient caseFormatterClient;

    @Autowired
    public CaseDataDraftToDivorceFormatter(CaseFormatterClient caseFormatterClient) {
        this.caseFormatterClient = caseFormatterClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseDataResponse) {
        Map<String, Object> formattedData = caseFormatterClient.transformToDivorceFormat(
                String.valueOf(context.getTransientObject(AUTH_TOKEN_JSON_KEY)),
                caseDataResponse
        );
        formattedData.put(IS_DRAFT_KEY, caseDataResponse.get(IS_DRAFT_KEY));
        formattedData.remove("expires");
        formattedData.remove("court");
        return formattedData;
    }
}