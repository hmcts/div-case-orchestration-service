package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;

public class AlternativeServiceDueDateSetterTaskTest extends DueDateSetterTaskTest {

    @Autowired
    private AlternativeServiceDueDateSetterTask alternativeServiceDueDateSetterTask;

    private static final Integer DUE_DATE_OFFSET = 7;

    @Override
    protected Integer getDaysBeforeOverdue() {
        return DUE_DATE_OFFSET;
    }

    @Override
    protected DueDateSetterTask getDueDateSetterTaskInstance() {
        return alternativeServiceDueDateSetterTask;
    }
}
