package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.MakeServiceDecisionDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.OrderToDispenseGenerationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
public class MakeServiceDecisionDateWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final MakeServiceDecisionDateTask makeServiceDecisionDateTask;
    private final OrderToDispenseGenerationTask orderToDispenseGenerationTask;

    public MakeServiceDecisionDateWorkflow(MakeServiceDecisionDateTask makeServiceDecisionDateTask,
                                           OrderToDispenseGenerationTask orderToDispenseGenerationTask) {
        this.makeServiceDecisionDateTask = makeServiceDecisionDateTask;
        this.orderToDispenseGenerationTask = orderToDispenseGenerationTask;
    }

    public Map<String, Object> run(CaseDetails caseDetails, String auth) throws WorkflowException {

        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} Make Service Decision workflow is going to be executed.", caseId);

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(makeServiceDecisionDateTask);

        if (isServiceApplicationDispensed(caseDetails.getCaseData())) {
            log.info("CaseID: {} application type = dispensed. Order to Dispense will be generated", caseId);
            tasks.add(orderToDispenseGenerationTask);
        } else {
            log.info("CaseID: {} application type is not dispensed. No pdf will be generated", caseId);
        }

        return this.execute(
            tasks.toArray(new Task[0]),
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, auth)
        );
    }

    protected boolean isServiceApplicationDispensed(Map<String, Object> caseData) {
        return "dispensed".equals(caseData.get(CcdFields.SERVICE_APPLICATION_TYPE));
    }
}
