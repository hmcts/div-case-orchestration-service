package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.exception.InvalidPropertyException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForDefendedDivorceEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForUndefendedDivorceEmail;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_DEFENDS_DIVORCE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.util.JsonPayloadUtils.getBooleanFromPayloadField;

@Component
@Slf4j
public class SendRespondentSubmissionNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private SendRespondentSubmissionNotificationForDefendedDivorceEmail sendRespondentSubmissionNotificationForDefendedDivorceEmailTask;

    @Autowired
    private SendRespondentSubmissionNotificationForUndefendedDivorceEmail sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask;

    public Map<String, Object> run(CreateEvent caseRequestDetails) throws WorkflowException {
        Map<String, Object> caseData = caseRequestDetails.getCaseDetails().getCaseData();

        Task[] tasks = getAppropriateTasks(caseData);

        return execute(tasks,
                caseData,
                ImmutablePair.of(CASE_ID_JSON_KEY, caseRequestDetails.getCaseDetails().getCaseId())
        );
    }

    private Task[] getAppropriateTasks(Map<String, Object> caseData) throws WorkflowException {
        boolean respondentDefendingDivorce;
        try {
            respondentDefendingDivorce = getBooleanFromPayloadField(caseData, RESP_DEFENDS_DIVORCE_CCD_FIELD);
        } catch (InvalidPropertyException e) {
            log.error("Error deciding which tasks to perform in workflow", e);
            throw new WorkflowException(e.getMessage(), e);
        }

        Task[] tasks;
        if (respondentDefendingDivorce) {
            tasks = getTasksForDefendedDivorce();
        } else {
            tasks = getTasksForUndefendedDivorce();
        }
        return tasks;
    }

    private Task[] getTasksForDefendedDivorce() {
        return new Task[]{sendRespondentSubmissionNotificationForDefendedDivorceEmailTask};
    }

    private Task[] getTasksForUndefendedDivorce() {
        return new Task[]{sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask};
    }

}