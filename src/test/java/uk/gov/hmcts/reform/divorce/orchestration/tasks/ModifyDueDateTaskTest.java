package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(SpringRunner.class)
@PropertySource(value = "classpath:application.yml")
public class ModifyDueDateTaskTest {

    private ModifyDueDateTask modifyDueDateTask;

    private static final Integer OFFSET = 10;

    @Before
    public void setup() {
        modifyDueDateTask = new ModifyDueDateTask(OFFSET);
    }

    @Test
    public void executeShouldAddPopulatedField() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> returnedCaseData = modifyDueDateTask.execute(context(), caseData);

        assertThat(returnedCaseData.isEmpty(), is(false));
        assertDueDateIsValid(returnedCaseData);
    }

    @Test
    public void getFieldNameReturnsDueDate() {
        assertThat(modifyDueDateTask.getFieldName(), is(CcdFields.DUE_DATE));
    }

    @Test
    public void getFormattedDateShouldReturnDatePlusOffsetInCcdFormat() {
        assertThat(modifyDueDateTask.getFormattedDate(), is(getCcdFormattedDueDate()));
    }

    private void assertDueDateIsValid(Map<String, Object> caseData) {
        assertThat(caseData.get(CcdFields.DUE_DATE), is(getCcdFormattedDueDate()));
    }

    private String getCcdFormattedDueDate() {
        return DateUtils.formatDateFromLocalDate(LocalDate.now().plus(OFFSET, ChronoUnit.DAYS));
    }
}
