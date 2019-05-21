package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.REASON_FOR_DIVORCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.SELECTED_COURT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow.SELECTED_COURT;

@Slf4j
@Component
public class CourtAllocationTask implements Task<Map<String, Object>> {

    @Autowired
    private CourtAllocator courtAllocator;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        log.trace("Will select a court for case.");

        String reasonForDivorce = (String) payload.get(REASON_FOR_DIVORCE_KEY);
        String selectedCourt = courtAllocator.selectCourtForGivenDivorceFact(reasonForDivorce);

        log.trace("Court {} selected for case.", selectedCourt);

        HashMap<String, Object> mapToReturn = new HashMap<>(payload);
        mapToReturn.put(SELECTED_COURT_KEY, selectedCourt);

        context.setTransientObject(SELECTED_COURT, selectedCourt);

        return mapToReturn;
    }

}