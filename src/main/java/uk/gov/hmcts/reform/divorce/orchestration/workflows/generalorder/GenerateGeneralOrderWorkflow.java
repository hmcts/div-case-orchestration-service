package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrderDraftRemovalTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrderGenerationTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class GenerateGeneralOrderWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GeneralOrderGenerationTask generalOrderGenerationTask;
    private final GeneralOrderDraftRemovalTask generalOrderDraftRemovalTask;

    public Map<String, Object> run(CaseDetails caseDetails, String auth) throws WorkflowException {
        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} Generate General Order Workflow is going to be executed.", caseId);

        return this.execute(
            new Task[] {generalOrderGenerationTask, generalOrderDraftRemovalTask},
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, auth)
        );
    }
}
