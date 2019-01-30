package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class CourtAllocationTask implements Task<Map<String, Object>> {

    private static final String SELECTED_COURT_KEY = "courts";

    @Autowired
    private CourtAllocator courtAllocator;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        log.trace("Will select a court for case.");

        Optional<String> reasonForDivorce = Optional.ofNullable((String) payload.get("reasonForDivorce"));
        String selectedCourt = courtAllocator.selectCourtForGivenDivorceReason(reasonForDivorce);

        log.info("Court {} selected for case.", selectedCourt);

        HashMap<String, Object> mapToReturn = new HashMap<>(payload);
        mapToReturn.put(SELECTED_COURT_KEY, selectedCourt);

        context.setTransientObject("selectedCourt", selectedCourt);

        return mapToReturn;
    }

}