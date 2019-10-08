package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.RespondentAosAnswersProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AosPackOfflineAnswersWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private RespondentAosAnswersProcessor respondentAosAnswersProcessor;

    public Map<String, Object> run(Map<String, Object> payload, DivorceParty divorceParty) throws WorkflowException {

        List<Task> tasks = new ArrayList<>();
        if (divorceParty.equals(DivorceParty.RESPONDENT)) {
            tasks.add(respondentAosAnswersProcessor);
        }

        return execute(tasks.toArray(new Task[] {}), payload);
    }

}