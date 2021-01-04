package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;

public class CalculateDecreeAbsoluteDatesTaskTest {

    private static final String DATE_RESPONDENT_ELIGIBLE_FOR_DA = "DateRespondentEligibleForDA";
    private static final String DATE_CASE_NO_LONGER_ELIGIBLE_FOR_DA = "DateCaseNoLongerEligibleForDA";

    private CalculateDecreeAbsoluteDatesTask calculateDecreeAbsoluteDatesTask;

    @Before
    public void setUp() {
        calculateDecreeAbsoluteDatesTask = new CalculateDecreeAbsoluteDatesTask();
    }

    @Test
    public void testDecreeAbsoluteDatesAreReturned() throws TaskException {
        Map<String, ?> returnedPayload = calculateDecreeAbsoluteDatesTask.execute(
            null, singletonMap(DECREE_NISI_GRANTED_DATE_CCD_FIELD, "2020-01-09")
        );

        assertThat(returnedPayload, allOf(
            hasEntry(DECREE_NISI_GRANTED_DATE_CCD_FIELD, "2020-01-09"),
            hasEntry(DATE_RESPONDENT_ELIGIBLE_FOR_DA, "2020-05-21"),
            hasEntry(DATE_CASE_NO_LONGER_ELIGIBLE_FOR_DA, "2021-01-09")
        ));
    }

    @Test
    public void testExceptionIsThrownWhenDateDoesNotExist() throws TaskException {
        Map<String, Object> payload = emptyMap();

        assertThrows(
            TaskException.class,
            () -> calculateDecreeAbsoluteDatesTask.execute(null, payload)
        );
    }

    @Test
    public void testTaskExceptionIsThrownWhenDateBadlyFormatted() throws TaskException {
        Map<String, Object> payload = singletonMap(DECREE_NISI_GRANTED_DATE_CCD_FIELD, "20190711");

        assertThrows(
            TaskException.class,
            () -> calculateDecreeAbsoluteDatesTask.execute(null, payload)
        );
    }
}
