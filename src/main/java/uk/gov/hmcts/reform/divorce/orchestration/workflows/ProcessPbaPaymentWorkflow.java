package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ProcessPbaPayment;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateSolicitorCaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class ProcessPbaPaymentWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ValidateSolicitorCaseData validateSolicitorCaseData;
    private final ProcessPbaPayment processPbaPayment;

    @Autowired
    public ProcessPbaPaymentWorkflow(ProcessPbaPayment processPbaPayment,
                                     ValidateSolicitorCaseData validateSolicitorCaseData) {
        this.validateSolicitorCaseData = validateSolicitorCaseData;
        this.processPbaPayment = processPbaPayment;
    }

    public Map<String, Object> run(CreateEvent caseDetailsRequest, String authToken) throws WorkflowException {

        return this.execute(new Task[] {
            validateSolicitorCaseData,
            processPbaPayment
        },
            caseDetailsRequest.getCaseDetails().getCaseData(),
            new ImmutablePair(AUTH_TOKEN_JSON_KEY, authToken),
            new ImmutablePair(CASE_ID_JSON_KEY, caseDetailsRequest.getCaseDetails().getCaseId())
        );
    }
}
