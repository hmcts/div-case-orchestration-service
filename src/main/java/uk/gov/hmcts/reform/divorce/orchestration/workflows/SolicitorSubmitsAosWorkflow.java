package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitRespondentAosCaseForSolicitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class SolicitorSubmitsAosWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SubmitRespondentAosCaseForSolicitor submitRespondentAosCaseForSolicitor;

    @Autowired
    public SolicitorSubmitsAosWorkflow(
            SubmitRespondentAosCaseForSolicitor submitRespondentAosCaseForSolicitor) {
        this.submitRespondentAosCaseForSolicitor = submitRespondentAosCaseForSolicitor;
    }

    public Map<String, Object> run(CaseDetails caseDetails, final String authToken) throws WorkflowException {

        List<Task> tasks = new ArrayList<>();

        if (isSolicitorRepresentingRespondent(caseDetails.getCaseData())) {
            tasks.add(submitRespondentAosCaseForSolicitor);
        }

        return this.execute(
                tasks.toArray(new Task[tasks.size()]),
                caseDetails.getCaseData(),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails)
        );
    }

    private boolean isSolicitorRepresentingRespondent(Map<String, Object> caseData) {
        final String respondentSolicitorRepresented = (String) caseData.get(RESP_SOL_REPRESENTED);
        return YES_VALUE.equalsIgnoreCase(respondentSolicitorRepresented);
    }

}
