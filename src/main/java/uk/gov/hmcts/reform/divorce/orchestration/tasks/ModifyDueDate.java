package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class ModifyDueDate implements Task<Map<String, Object>> {

    @Value("${bulk-print.dueDate}")
    private Integer dueDate;

    private static final String DUE_DATE = "dueDate";

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload)  {
        LocalDate plus9Days = LocalDate.now().plus(dueDate, ChronoUnit.DAYS);
        payload.put(DUE_DATE, plus9Days.format(DateTimeFormatter.ISO_LOCAL_DATE));
        return payload;
    }
}
