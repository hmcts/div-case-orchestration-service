package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class DueDateSetterTaskTest {

    private DueDateSetterTask dueDateSetterTask;
    private Integer offset;

    @Before
    public void setup() {
        offset = getDaysBeforeOverdue();
        dueDateSetterTask = getDueDateSetterTaskInstance();
    }

    protected abstract Integer getDaysBeforeOverdue();

    protected abstract DueDateSetterTask getDueDateSetterTaskInstance();

    @Test
    public void gettersReturnExpectedValues() {
        assertThat(dueDateSetterTask.getDueDateOffsetInDays(), is(offset));
        assertThat(dueDateSetterTask.getFieldName(), is(CcdFields.DUE_DATE));
    }

}
