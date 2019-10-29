package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.CourtLookupService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURTS;

@Component
public class AddCourtsToPayloadTask implements Task<Map<String, Object>> {

    @Autowired
    private CourtLookupService courtLookupService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        Map<String, Object> payloadToReturn = new HashMap<>(payload);

        payloadToReturn.put(COURTS, courtLookupService.getAllCourts());

        return payloadToReturn;
    }

}