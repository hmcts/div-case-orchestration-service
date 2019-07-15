package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PRONOUNCEMENT_JUDGE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SetDaGrantedDetailsTaskTest {

    @Mock private Clock clock;

    @InjectMocks
    private SetDaGrantedDetailsTask setDaGrantedDetailsTask;

    private static final String EXPECTED_DATE_TIME = "2019-06-30T10:00:00.000";

    @Before
    public void setup() {
        when(clock.instant()).thenReturn(LocalDateTime.of(
                2019, 06, 30, 10, 00, 00).toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(UTC);
    }

    @Test(expected = TaskException.class)
    public void shouldThrowExceptionForSetDaGrantedDetailsTaskIfPronouncementJudgeIsEmpty() throws TaskException {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put(COURT_HEARING_DATE_CCD_FIELD, EXPECTED_DATE_TIME);

        setDaGrantedDetailsTask.execute(null, payload);
    }

    @Test
    public void shouldSetsTheDateFieldsProperlyForSetDaGrantedDetailsIfPronouncementJudgeIsGiven() throws TaskException {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put(PRONOUNCEMENT_JUDGE_CCD_FIELD, TEST_PRONOUNCEMENT_JUDGE);

        setDaGrantedDetailsTask.execute(null, payload);

        assertEquals(EXPECTED_DATE_TIME, payload.get(DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD));
    }
}
