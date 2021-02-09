package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AosPackDueDateSetterTask extends DueDateSetterTask {

    private final Integer dueDateOffset;

    public AosPackDueDateSetterTask(@Value("${bulk-print.dueDate}") Integer dueDateOffset) {
        this.dueDateOffset = dueDateOffset;
    }

    @Override
    protected Integer getDueDateOffsetInDays() {
        return this.dueDateOffset;
    }
}
