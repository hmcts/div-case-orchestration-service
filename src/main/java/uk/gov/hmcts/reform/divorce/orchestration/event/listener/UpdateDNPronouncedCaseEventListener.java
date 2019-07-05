package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.UpdateDNPronouncedCaseEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASES_ELIGIBLE_FOR_DA_PROCESSED_COUNT;

@Component
@Slf4j
public class UpdateDNPronouncedCaseEventListener implements ApplicationListener<UpdateDNPronouncedCaseEvent> {

    @Autowired
    private CaseOrchestrationService caseOrchestrationService;

    @Override
    public void onApplicationEvent(UpdateDNPronouncedCaseEvent event) {
        log.info("reached listener for UpdateDNPronouncedCaseEvent");
        try {
            caseOrchestrationService.makeCaseEligibleForDA(event.getAuthToken(), event.getCaseId());
            updateCasesProcessedCount((TaskContext) event.getSource());
            log.info(String.format("UpdateDNPronouncedCaseEvent [%s] has been processed", event.getCaseId()));
        } catch (CaseOrchestrationServiceException e) {
            log.error(String.format("Exception thrown while processing UpdateDNPronouncedCaseEvent [%s]: %s",
                    event.getCaseId(), e.getMessage()), e);
        }
    }

    private void updateCasesProcessedCount(TaskContext taskContext) {
        int currentCount = taskContext.getTransientObject(CASES_ELIGIBLE_FOR_DA_PROCESSED_COUNT);
        taskContext.setTransientObject(CASES_ELIGIBLE_FOR_DA_PROCESSED_COUNT, currentCount + 1);
    }
}
