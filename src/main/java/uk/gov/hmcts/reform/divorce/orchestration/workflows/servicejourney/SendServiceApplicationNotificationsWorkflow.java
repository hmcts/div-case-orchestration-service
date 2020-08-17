package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedApprovedEmailNotificationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDeemed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationGranted;

@Component
@Slf4j
@RequiredArgsConstructor
public class SendServiceApplicationNotificationsWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final DeemedApprovedEmailNotificationTask deemedApprovedEmailNotificationTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} service decision made emails are going to be sent.", caseId);

        Map<String, Object> caseData = caseDetails.getCaseData();
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        if (isServiceApplicationGranted(caseData)) {
            if (isServiceApplicationDeemed(caseData)) {
                log.info("CaseId: {} deemed citizen email task adding.", caseId);
                tasks.add(deemedApprovedEmailNotificationTask);
            } else {
                log.info("CaseId: {} NOT deemed. To be implemented", caseId);
            }
        } else {
            log.info("CaseId: {} NOT granted. To be implemented", caseId);
        }

        return this.execute(
            tasks.toArray(new Task[0]),
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
