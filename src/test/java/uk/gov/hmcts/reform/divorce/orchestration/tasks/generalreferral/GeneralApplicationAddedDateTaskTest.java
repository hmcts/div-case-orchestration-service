package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class GeneralApplicationAddedDateTaskTest extends TestCase {

    @InjectMocks
    private GeneralApplicationAddedDateTask generalApplicationAddedDateTask;

    @Test
    public void executeShouldAddPopulatedField() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> returnedCaseData = generalApplicationAddedDateTask
            .execute(context(), caseData);

        assertThat(returnedCaseData.isEmpty(), is(false));
        assertSame(returnedCaseData, caseData);
        assertThat(
            returnedCaseData.get(CcdFields.GENERAL_APPLICATION_ADDED_DATE),
            is(DateUtils.formatDateFromLocalDate(LocalDate.now()))
        );
    }
}
