package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

@Component
public class FormatDivorceSessionToCaseData implements Task<Map<String, Object>> {

    @Autowired
    private CaseFormatterClient caseFormatterClient;

    private String authToken;

    public void setup(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> sessionData) throws TaskException {
        try {
            return caseFormatterClient.transformToCCDFormat(sessionData, authToken);
        } catch (Exception exception) {
            throw new TaskException(exception.getMessage());
        }
    }
}
