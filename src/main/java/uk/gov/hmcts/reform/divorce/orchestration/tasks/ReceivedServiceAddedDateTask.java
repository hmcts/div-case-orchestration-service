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
public class ReceivedServiceAddedDateTask implements Task<Map<String, Object>> {

    public static final String RECEIVED_SERVICE_ADDED_DATE = "ReceivedServiceAddedDate";

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        payload.put(RECEIVED_SERVICE_ADDED_DATE, DateUtils.formatDateFromLocalDate(LocalDate.now()));

        return payload;
    }
}
