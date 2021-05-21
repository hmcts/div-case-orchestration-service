package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalemail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.StoreGeneralEmailFieldsTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoreGeneralEmailFieldsWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final StoreGeneralEmailFieldsTask storeGeneralEmailFieldsTask;

    public Map<String, Object> run(CaseDetails caseDetails, String authorizationToken) throws WorkflowException {
        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        log.info("CaseID: {} StoreGeneralEmailFieldsWorkflow workflow is going to be executed.", caseId);

        return this.execute(
            new Task[] { storeGeneralEmailFieldsTask },
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authorizationToken)
        );
    }
}
