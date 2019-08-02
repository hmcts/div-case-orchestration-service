package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

@Component
public class IssuePersonalServicePackWorkflow extends DefaultWorkflow<Map<String, Object>> {
    public Map<String, Object> run(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException {
        return null;
    }
}
