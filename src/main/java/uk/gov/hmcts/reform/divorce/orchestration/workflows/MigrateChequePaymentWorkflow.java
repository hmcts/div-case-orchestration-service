package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MigrateChequeTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
public class MigrateChequePaymentWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final MigrateChequeTask migrateChequeTask;

    @Autowired
    public MigrateChequePaymentWorkflow(MigrateChequeTask migrateChequeTask) {
        this.migrateChequeTask = migrateChequeTask;
    }

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        log.info("CaseID: {} MigrateChequePaymentWorkflow is going to be executed.", caseId);

        return this.execute(
            new Task[] {
                migrateChequeTask
            },
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, ccdCallbackRequest.getCaseDetails()),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, ccdCallbackRequest.getEventId())
        );
    }
}
