package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.BailiffUnsuccessServiceDueDateSetterTask;

public class BailiffUnsuccessServiceDueDateSetterTaskTest extends DueDateSetterTaskTest {

    @Autowired
    private BailiffUnsuccessServiceDueDateSetterTask bailiffunSuccessServiceDueDateSetterTask;

    private static final Integer DUE_DATE_OFFSET = 30;

    @Override
    protected Integer getDaysBeforeOverdue() {
        return DUE_DATE_OFFSET;
    }

    @Override
    protected DueDateSetterTask getDueDateSetterTaskInstance() {
        return bailiffunSuccessServiceDueDateSetterTask;
    }
}
