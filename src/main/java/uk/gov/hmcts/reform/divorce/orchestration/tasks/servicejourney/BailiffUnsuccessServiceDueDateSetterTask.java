package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BailiffUnsuccessServiceDueDateSetterTask extends BailiffServiceDueDateSetterTask {

    public BailiffUnsuccessServiceDueDateSetterTask(@Value("${bailiff.unsuccessful.dueDate}") Integer dueDateOffset) {
        super(dueDateOffset);
    }
}
