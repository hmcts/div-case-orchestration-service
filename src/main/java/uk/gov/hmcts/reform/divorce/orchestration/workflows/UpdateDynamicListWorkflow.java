package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDNcostOptions;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COST_OPTIONS_DN;

@Component
public class UpdateDynamicListWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private SetDNcostOptions setDNCostOptions;

    public Map<String, Object> run(CaseDetails caseDetails, String dynamicListIdInCcd) throws WorkflowException {

        return this.execute(
                new Task[] {
                    findTaskToBeExecuted(dynamicListIdInCcd)
                },
            caseDetails.getCaseData()
        );
    }

    private Task findTaskToBeExecuted(String dynamicListIdInCcd) {
        if (StringUtils.equalsIgnoreCase(dynamicListIdInCcd, DIVORCE_COST_OPTIONS_DN)) {
            return setDNCostOptions;
        }

        return null;
    }
}
