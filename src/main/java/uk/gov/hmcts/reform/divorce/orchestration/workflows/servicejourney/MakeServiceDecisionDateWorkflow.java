package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.MakeServiceDecisionDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.OrderToDispenseGenerationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDeemed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDispensed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationGranted;

@Component
@Slf4j
public class MakeServiceDecisionDateWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final MakeServiceDecisionDateTask makeServiceDecisionDateTask;
    private final OrderToDispenseGenerationTask orderToDispenseGenerationTask;
    private final DeemedServiceOrderGenerationTask deemedServiceOrderGenerationTask;

    public MakeServiceDecisionDateWorkflow(MakeServiceDecisionDateTask makeServiceDecisionDateTask,
                                           OrderToDispenseGenerationTask orderToDispenseGenerationTask,
                                           DeemedServiceOrderGenerationTask deemedServiceOrderGenerationTask) {
        this.makeServiceDecisionDateTask = makeServiceDecisionDateTask;
        this.orderToDispenseGenerationTask = orderToDispenseGenerationTask;
        this.deemedServiceOrderGenerationTask = deemedServiceOrderGenerationTask;
    }

    public Map<String, Object> run(CaseDetails caseDetails, String auth) throws WorkflowException {

        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        log.info("CaseID: {} Make Service Decision workflow is going to be executed.", caseId);

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(makeServiceDecisionDateTask);

        if (isServiceApplicationGranted(caseData)) {
            if (isServiceApplicationDispensed(caseData)) {
                log.info("CaseID: {} application type = dispensed. Order to Dispense will be generated.", caseId);
                tasks.add(orderToDispenseGenerationTask);
            } else if (isServiceApplicationDeemed(caseData)) {
                log.info("CaseID: {} application type = deemed. Deemed Service Order will be generated.", caseId);
                tasks.add(deemedServiceOrderGenerationTask);
            } else {
                log.info("CaseID: {} application type != dispensed/deemed. No pdf will be generated.", caseId);
            }
        } else {
            log.info("CaseID: {} Service application is not granted. No pdf will be generated.", caseId);
        }

        return this.execute(
            tasks.toArray(new Task[0]),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, auth)
        );
    }
}
