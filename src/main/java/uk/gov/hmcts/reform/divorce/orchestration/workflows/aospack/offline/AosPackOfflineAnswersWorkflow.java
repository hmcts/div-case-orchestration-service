package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.CoRespondentAosAnswersProcessorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.FormFieldValuesToCoreFieldsRelay;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.RespondentAosAnswersProcessorTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;

@Component
public class AosPackOfflineAnswersWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private RespondentAosAnswersProcessorTask respondentAosAnswersProcessor;
    private FormFieldValuesToCoreFieldsRelay formFieldValuesToCoreFieldsRelay;
    private CoRespondentAosAnswersProcessorTask coRespondentAosAnswersProcessorTask;


    @Autowired
    public AosPackOfflineAnswersWorkflow(RespondentAosAnswersProcessorTask respondentAosAnswersProcessor,
                                         FormFieldValuesToCoreFieldsRelay formFieldValuesToCoreFieldsRelay,
                                         CoRespondentAosAnswersProcessorTask coRespondentAosAnswersProcessorTask) {
        this.respondentAosAnswersProcessor = respondentAosAnswersProcessor;
        this.formFieldValuesToCoreFieldsRelay = formFieldValuesToCoreFieldsRelay;
        this.coRespondentAosAnswersProcessorTask = coRespondentAosAnswersProcessorTask;
    }

    public Map<String, Object> run(Map<String, Object> payload, DivorceParty divorceParty) throws WorkflowException {

        List<Task> tasks = getTasks(divorceParty);

        return execute(tasks.toArray(new Task[] {}), payload);
    }

    private List<Task> getTasks(DivorceParty divorceParty) {
        List<Task> tasks = new ArrayList<>();

        tasks.add(formFieldValuesToCoreFieldsRelay);

        if (RESPONDENT.equals(divorceParty)) {
            tasks.add(respondentAosAnswersProcessor);
        } else if(CO_RESPONDENT.equals(divorceParty)) {
            tasks.add(coRespondentAosAnswersProcessorTask);
        }

        return tasks;
    }

}

/*
private Task<Map<String, Object>>[] getTasks(DivorceParty divorceParty) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();
        tasks.add(formFieldValuesToCoreFieldsRelay);

        if (RESPONDENT.equals(divorceParty)) {
            tasks.add(respondentAosAnswersProcessor);
        } else if(CO_RESPONDENT.equals(divorceParty)) {
            tasks.add(coRespondentAosAnswersProcessorTask);
        }

        Task<Map<String, Object>>[] arr = new Task[tasks.size()];
        return tasks.toArray(arr);
    }

 */