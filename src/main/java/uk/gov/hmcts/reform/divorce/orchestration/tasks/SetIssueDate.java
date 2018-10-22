package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.time.LocalDate;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ISSUE_DATE;

@Component
public class SetIssueDate implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        payload.put(ISSUE_DATE, LocalDate.now().format(ofPattern(CCD_DATE_FORMAT)));
        return payload;
    }
}
