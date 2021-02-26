package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.BailiffSuccessServiceDueDateSetterTask;

public class BailiffSuccessServiceDueDateSetterTaskTest extends DueDateSetterTaskTest {

    @Autowired
    private BailiffSuccessServiceDueDateSetterTask bailiffSuccessServiceDueDateSetterTask;

    private static final Integer DUE_DATE_OFFSET = 7;

    @Override
    protected Integer getDaysBeforeOverdue() {
        return DUE_DATE_OFFSET;
    }

    @Override
    protected DueDateSetterTask getDueDateSetterTaskInstance() {
        return bailiffSuccessServiceDueDateSetterTask;
    }
}
