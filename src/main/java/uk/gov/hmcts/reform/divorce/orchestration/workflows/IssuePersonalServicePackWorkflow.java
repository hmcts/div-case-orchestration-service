package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;

import java.util.Map;

@Component
public class IssuePersonalServicePackWorkflow extends DefaultWorkflow<Map<String, Object>> {
    public Map<String, Object> run() {
        return null;
    }
}
