package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerSubmissionNotificationEmailTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@RequiredArgsConstructor
public class SendPetitionerSubmissionNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendPetitionerSubmissionNotificationEmailTask sendPetitionerSubmissionNotificationEmailTask;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        return this.execute(
            new Task[] {
                sendPetitionerSubmissionNotificationEmailTask,
            },
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, ccdCallbackRequest.getCaseDetails().getCaseId())
        );
    }
}
