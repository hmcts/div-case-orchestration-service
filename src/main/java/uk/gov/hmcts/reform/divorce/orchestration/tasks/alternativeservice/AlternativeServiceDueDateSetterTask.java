package uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DueDateSetterTask;

@Component
public class AlternativeServiceDueDateSetterTask extends DueDateSetterTask {

    private final Integer dueDateOffset;

    public AlternativeServiceDueDateSetterTask(@Value("${alternative-service.days-until-overdue}") Integer dueDateOffset) {
        this.dueDateOffset = dueDateOffset;
    }

    @Override
    protected Integer getDueDateOffsetInDays() {
        return this.dueDateOffset;
    }
}
