package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetCoRespondentAnswerReceived;

import java.util.Map;

@Component
@AllArgsConstructor
public class CoRespondentAnswerReceivedWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private final  SetCoRespondentAnswerReceived setCoRespondentAnswerReceived;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {

        return this.execute(new Task[]{setCoRespondentAnswerReceived},
            caseDetails.getCaseData());
    }

}
