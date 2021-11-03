package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveLegalAdvisorMakeDecisionFieldsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.PetitionerClarificationSubmittedNotificationEmailTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendClarificationSubmittedNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final RemoveLegalAdvisorMakeDecisionFieldsTask removeLegalAdvisorMakeDecisionFieldsTask;
    private final PetitionerClarificationSubmittedNotificationEmailTask sendPetitionerNotificationEmail;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();
        log.info("Clarification submitted notification workflow for CASE ID: {} ", caseId);
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(removeLegalAdvisorMakeDecisionFieldsTask);
        tasks.add(sendPetitionerNotificationEmail);

        return this.execute(
            tasks.toArray(new Task[tasks.size()]),
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
