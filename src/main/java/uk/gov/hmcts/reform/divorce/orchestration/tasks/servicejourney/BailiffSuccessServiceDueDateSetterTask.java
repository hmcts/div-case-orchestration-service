package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BailiffSuccessServiceDueDateSetterTask extends BailiffServiceDueDateSetterTask {

    public BailiffSuccessServiceDueDateSetterTask(@Value("${bailiff.successful.dueDate}") Integer dueDateOffset) {
        super(dueDateOffset);
    }
}
