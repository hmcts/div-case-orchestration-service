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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_REQUESTED_DATE_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SetDaRequestedDetailsTaskTest {

    @Mock private Clock clock;

    @InjectMocks
    private SetDaRequestedDetailsTask setDaRequestedDetailsTask;

    private static final String EXPECTED_DATE_TIME = "2019-08-27T10:00:00.000";

    @Before
    public void setup() {
        when(clock.instant()).thenReturn(LocalDateTime.of(
                2019, 8, 27, 10, 00, 00).toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(UTC);
    }

    @Test
    public void shouldSetTheDateFieldsProperlyForSetDaRequestedDetails() throws TaskException {
        HashMap<String, Object> payload = new HashMap<>();

        setDaRequestedDetailsTask.execute(null, payload);

        assertEquals(EXPECTED_DATE_TIME, payload.get(DECREE_ABSOLUTE_REQUESTED_DATE_CCD_FIELD));
    }
}
