package uk.gov.hmcts.reform.divorce.orchestration.workflows.decreeabsolute;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CalculateDecreeAbsoluteDates;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.NotifyApplicantCanFinaliseDivorceTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class ApplicantDecreeAbsoluteEligibilityWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private CalculateDecreeAbsoluteDates calculateDecreeAbsoluteDates;

    @Autowired
    private NotifyApplicantCanFinaliseDivorceTask notifyApplicantCanFinaliseDivorceTask;

    public Map<String, Object> run(String caseId, Map<String, Object> payload) throws WorkflowException {
        return execute(
            new Task[] {
                calculateDecreeAbsoluteDates,
                notifyApplicantCanFinaliseDivorceTask
            },
            payload,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId));
    }

}