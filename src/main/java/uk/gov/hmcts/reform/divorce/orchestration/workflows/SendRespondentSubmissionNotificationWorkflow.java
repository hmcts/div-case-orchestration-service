package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForDefendedDivorceEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForUndefendedDivorceEmail;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@Slf4j
public class SendRespondentSubmissionNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private SendRespondentSubmissionNotificationForDefendedDivorceEmail
        sendRespondentSubmissionNotificationForDefendedDivorceEmailTask;

    @Autowired
    private SendRespondentSubmissionNotificationForUndefendedDivorceEmail
        sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        String defended = (String) caseData.get(RESP_WILL_DEFEND_DIVORCE);

        Task[] tasks;

        if (YES_VALUE.equalsIgnoreCase(defended)) {
            tasks = new Task[] {sendRespondentSubmissionNotificationForDefendedDivorceEmailTask};
        } else if (NO_VALUE.equalsIgnoreCase(defended)) {
            tasks = new Task[] {sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask};
        } else {
            String errorMessage = String.format("%s field doesn't contain a valid value: %s",
                RESP_WILL_DEFEND_DIVORCE, defended);
            log.error(String.format("%s. %n Case id: %s.", errorMessage, ccdCallbackRequest.getCaseDetails().getCaseId()));
            throw new WorkflowException(errorMessage);
        }

        return execute(tasks,
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, ccdCallbackRequest.getCaseDetails().getCaseId())
        );
    }
}