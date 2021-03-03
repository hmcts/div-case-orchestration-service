package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.DateCalculator;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

public class AosPackDueDateSetterTaskTest extends DueDateSetterTaskTest {

    @Autowired
    private AosPackDueDateSetterTask aosPackDueDateSetterTask;

    private static final Integer DUE_DATE_OFFSET = 30;

    @Override
    protected Integer getDaysBeforeOverdue() {
        return DUE_DATE_OFFSET;
    }

    @Override
    protected DueDateSetterTask getDueDateSetterTaskInstance() {
        return aosPackDueDateSetterTask;
    }

    @Test
    public void executeShouldAddPopulatedFieldWithExpectedValue() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> returnedCaseData = aosPackDueDateSetterTask.execute(context(), caseData);

        assertThat(returnedCaseData.isEmpty(), is(false));
        assertThat(caseData, hasEntry(CcdFields.DUE_DATE, getDatePlusOffsetInCcdFormat()));
    }

    private String getDatePlusOffsetInCcdFormat() {
        return DateCalculator.getDateWithOffset(DUE_DATE_OFFSET);
    }
}
