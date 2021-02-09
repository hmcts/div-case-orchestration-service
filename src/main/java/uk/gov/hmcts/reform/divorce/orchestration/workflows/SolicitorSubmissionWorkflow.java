package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ProcessPbaPaymentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveMiniPetitionDraftDocumentsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerSubmissionNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateSolicitorCaseDataTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RequiredArgsConstructor
@Component
public class SolicitorSubmissionWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ValidateSolicitorCaseDataTask validateSolicitorCaseDataTask;
    private final ProcessPbaPaymentTask processPbaPaymentTask;
    private final RemoveMiniPetitionDraftDocumentsTask removeMiniPetitionDraftDocumentsTask;
    private final SendPetitionerSubmissionNotificationEmailTask sendPetitionerSubmissionNotificationEmailTask;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {

        return this.execute(
            new Task[] {
                validateSolicitorCaseDataTask,
                processPbaPaymentTask,
                removeMiniPetitionDraftDocumentsTask,
                sendPetitionerSubmissionNotificationEmailTask
            },
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, ccdCallbackRequest.getCaseDetails().getCaseId())
        );
    }
}
