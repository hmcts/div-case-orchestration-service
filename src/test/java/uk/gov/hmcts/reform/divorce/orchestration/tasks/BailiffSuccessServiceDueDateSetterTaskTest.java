package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.BailiffSuccessServiceDueDateSetterTask;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.CERTIFICATE_OF_SERVICE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

public class BailiffSuccessServiceDueDateSetterTaskTest extends DueDateSetterTaskTest {

    @Autowired
    private BailiffSuccessServiceDueDateSetterTask bailiffSuccessServiceDueDateSetterTask;

    private static final Integer DUE_DATE_OFFSET = 7;
    private static final String COS_DATE = "2020-11-10";

    @Override
    protected Integer getDaysBeforeOverdue() {
        return DUE_DATE_OFFSET;
    }

    @Override
    protected DueDateSetterTask getDueDateSetterTaskInstance() {
        return bailiffSuccessServiceDueDateSetterTask;
    }

    @Test
    public void getFormattedDateReturnCorrectDate() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CERTIFICATE_OF_SERVICE_DATE, COS_DATE);
        String newDueDate = LocalDate.parse((String) caseData.get(CERTIFICATE_OF_SERVICE_DATE))
                .plusDays(DUE_DATE_OFFSET).toString();

        Map<String, Object> resultData = bailiffSuccessServiceDueDateSetterTask.execute(context(), caseData);

        assertThat(caseData, hasEntry(CcdFields.DUE_DATE, newDueDate));

        assertThat(resultData.get(DUE_DATE), is(newDueDate));
    }

}
