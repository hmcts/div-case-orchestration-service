package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CCD_CASE_DATA;

@Component
public class CaseDataToDivorceFormatter implements Task<Map<String, Object>> {
    private final CaseFormatterClient caseFormatterClient;

    @Autowired
    public CaseDataToDivorceFormatter(CaseFormatterClient caseFormatterClient) {
        this.caseFormatterClient = caseFormatterClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> incomingCaseData) {
        return caseFormatterClient.transformToDivorceFormat(
            context.getTransientObject(AUTH_TOKEN_JSON_KEY),
            context.getTransientObject(CCD_CASE_DATA)
        );
    }

}