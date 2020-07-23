package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ReceivedServiceAddedDateTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReceivedServiceAddedDateWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ReceivedServiceAddedDateTask receivedServiceAddedDateTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {

        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} received service added date workflow is going to be executed.", caseId);

        this.execute(
            new Task[] {
                receivedServiceAddedDateTask
            },
            caseDetails.getCaseData()
        );

        log.info("CaseID: {} received service added date setup {}.", caseId, getReceivedServiceAddedDate(caseDetails));

        return caseDetails.getCaseData();
    }

    private String getReceivedServiceAddedDate(CaseDetails caseDetails) {
        return getMandatoryStringValue(caseDetails.getCaseData(), ReceivedServiceAddedDateTask.RECEIVED_SERVICE_ADDED_DATE);
    }
}
