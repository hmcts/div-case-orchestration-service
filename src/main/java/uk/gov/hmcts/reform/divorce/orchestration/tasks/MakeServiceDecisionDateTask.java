package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MakeServiceDecisionDateTask implements Task<Map<String, Object>> {

    public static final String SERVICE_APPLICATION_DECISION_DATE = "ServiceApplicationDecisionDate";

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        payload.put(SERVICE_APPLICATION_DECISION_DATE, DateUtils.formatDateFromLocalDate(LocalDate.now()));

        return payload;
    }
}
