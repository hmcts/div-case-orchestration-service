package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CREATED_DATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SetCourtDetailsTest {

    private final LocalDateTime today = LocalDateTime.now();

    @InjectMocks
    private SetCourtDetails setCourtDetails;

    @Mock
    private Clock clock;

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        when(clock.instant()).thenReturn(today.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(UTC);

        testData = new HashMap<>();
        context = new DefaultTaskContext();
    }

    @Test
    public void executeShouldSetDateAndCourtDetailsOnPayload() {
        Map<String, Object> resultData = new HashMap<>();
        resultData.put(CREATED_DATE_JSON_KEY, today.format(DateTimeFormatter.ofPattern(CCD_DATE_FORMAT)));
        resultData.put(DIVORCE_UNIT_JSON_KEY, CourtEnum.EASTMIDLANDS.getId());
        resultData.put(DIVORCE_CENTRE_SITEID_JSON_KEY, CourtEnum.EASTMIDLANDS.getSiteId());

        assertEquals(resultData, setCourtDetails.execute(context, testData));
    }
}
