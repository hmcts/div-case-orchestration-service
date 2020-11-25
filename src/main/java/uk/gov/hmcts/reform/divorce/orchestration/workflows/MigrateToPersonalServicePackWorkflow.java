package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CourtServiceValidationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MigrateCaseToPersonalServiceTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;


@Slf4j
@Component
@RequiredArgsConstructor
public class MigrateToPersonalServicePackWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final CourtServiceValidationTask courtServiceValidationTask;
    private final MigrateCaseToPersonalServiceTask migrateCaseToPersonalServiceTask;

    public Map<String, Object> run(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException {

        return this.execute(
            new Task[]{
                courtServiceValidationTask,
                migrateCaseToPersonalServiceTask,
            },
            callbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, callbackRequest.getCaseDetails().getCaseId()),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, callbackRequest.getCaseDetails())
        );
    }
}
