package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;

public class CalculateDecreeAbsoluteDatesTest {

    private static final String DATE_RESPONDENT_ELIGIBLE_FOR_DA = "DateRespondentEligibleForDA";
    private static final String DATE_CASE_NO_LONGER_ELIGIBLE_FOR_DA = "DateCaseNoLongerEligibleForDA";

    @Rule
    public ExpectedException expectedException = none();

    private CalculateDecreeAbsoluteDates calculateDecreeAbsoluteDates;

    @Before
    public void setUp() {
        calculateDecreeAbsoluteDates = new CalculateDecreeAbsoluteDates();
    }

    @Test
    public void testDecreeAbsoluteDatesAreReturned() throws TaskException {
        Map<String, ?> returnedPayload = calculateDecreeAbsoluteDates.execute(null, singletonMap(DECREE_NISI_GRANTED_DATE_CCD_FIELD, "2019-07-11"));

        assertThat(returnedPayload, allOf(
            hasEntry(DECREE_NISI_GRANTED_DATE_CCD_FIELD, "2019-07-11"),
            hasEntry(DATE_RESPONDENT_ELIGIBLE_FOR_DA, "2019-11-23"),
            hasEntry(DATE_CASE_NO_LONGER_ELIGIBLE_FOR_DA, "2020-07-11")
        ));
    }

    @Test
    public void testExceptionIsThrownWhenDateDoesNotExist() throws TaskException {
        expectedException.expect(TaskException.class);

        calculateDecreeAbsoluteDates.execute(null, emptyMap());
    }

    @Test
    public void testTaskExceptionIsThrownWhenDateBadlyFormatted() throws TaskException {
        expectedException.expect(TaskException.class);

        calculateDecreeAbsoluteDates.execute(null, singletonMap(DECREE_NISI_GRANTED_DATE_CCD_FIELD, "20190711"));
    }

}