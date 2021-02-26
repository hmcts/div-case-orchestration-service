package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.BailiffSuccessServiceDueDateSetterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.BailiffUnsuccessServiceDueDateSetterTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isBailiffServiceSuccessful;

@Component
@Slf4j
@RequiredArgsConstructor
public class BailiffOutcomeWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final BailiffSuccessServiceDueDateSetterTask bailiffSuccessServiceDueDateSetterTask;
    private final BailiffUnsuccessServiceDueDateSetterTask bailiffUnsuccessServiceDueDateSetterTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {}. Bailiff outcome workflow is going to be executed.", caseId);

        return this.execute(
            getTasks(caseDetails),
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        if (isBailiffServiceSuccessful(caseData)) {
            log.info("CaseID: {}. Certificate of service due date set to +7 days ", caseId);
            tasks.add(bailiffSuccessServiceDueDateSetterTask);
        } else {
            log.info("CaseID: {}. Certificate of service due date set to +30 days", caseId);
            tasks.add(bailiffUnsuccessServiceDueDateSetterTask);
        }

        return tasks.toArray(new Task[] {});
    }

}
