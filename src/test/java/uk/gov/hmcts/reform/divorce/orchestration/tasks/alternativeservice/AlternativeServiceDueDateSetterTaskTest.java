package uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DueDateSetterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DueDateSetterTaskTest;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.DateCalculator;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

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

    @Test
    public void executeShouldAddPopulatedFieldWithExpectedValue() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> returnedCaseData = alternativeServiceDueDateSetterTask.execute(context(), caseData);

        assertThat(returnedCaseData.isEmpty(), is(false));
        assertThat(caseData, hasEntry(CcdFields.DUE_DATE, getDatePlusOffsetInCcdFormat()));
    }

    private String getDatePlusOffsetInCcdFormat() {
        return DateCalculator.getDateWithOffset(DUE_DATE_OFFSET);
    }
}
