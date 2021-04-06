package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.DateCalculator;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.CERTIFICATE_OF_SERVICE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

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
    public void executeShouldAddPopulatedField() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CERTIFICATE_OF_SERVICE_DATE, LocalDate.now().toString());
        Map<String, Object> returnedCaseData = dueDateSetterTask.execute(context(), caseData);

        assertThat(returnedCaseData.isEmpty(), is(false));
        assertThat(caseData, hasEntry(CcdFields.DUE_DATE, getDatePlusOffsetInCcdFormat()));
    }

    @Test
    public void gettersReturnExpectedValues() {
        assertThat(dueDateSetterTask.getDueDateOffsetInDays(), is(offset));
        assertThat(dueDateSetterTask.getFieldName(), is(CcdFields.DUE_DATE));
        assertThat(dueDateSetterTask.getFormattedDate(), is(getDatePlusOffsetInCcdFormat()));
    }

    private String getDatePlusOffsetInCcdFormat() {
        return DateCalculator.getDateWithOffset(offset);
    }
}
