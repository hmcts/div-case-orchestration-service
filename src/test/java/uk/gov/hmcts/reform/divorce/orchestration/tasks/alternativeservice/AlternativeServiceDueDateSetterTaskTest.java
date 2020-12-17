package uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DueDateSetterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DueDateSetterTaskTest;

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
