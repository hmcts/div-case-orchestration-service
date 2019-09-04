package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitRespondentAosCaseForSolicitor;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@Component
public class UpdateDataWhenSolicitorSubmitsWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SubmitRespondentAosCaseForSolicitor submitRespondentAosCaseForSolicitor;

    @Autowired
    public UpdateDataWhenSolicitorSubmitsWorkflow(
            SubmitRespondentAosCaseForSolicitor submitRespondentAosCaseForSolicitor) {
        this.submitRespondentAosCaseForSolicitor = submitRespondentAosCaseForSolicitor;
    }

    public Map<String, Object> run(CaseDetails caseDetails, final String authToken) throws WorkflowException {

        return this.execute(
                new Task[]{
                    submitRespondentAosCaseForSolicitor
                },
                caseDetails.getCaseData(),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails)
        );
    }
}
