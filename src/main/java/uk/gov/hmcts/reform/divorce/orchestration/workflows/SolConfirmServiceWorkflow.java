package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PersonalServiceValidationTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class SolConfirmServiceWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final PersonalServiceValidationTask personalServiceValidationTask;

    public Map<String, Object> run(final CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        final CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        return this.execute(
            new Task[]{personalServiceValidationTask},
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId()),
            ImmutablePair.of(CASE_STATE_JSON_KEY, caseDetails.getState())
        );
    }
}