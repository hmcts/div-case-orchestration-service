package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ISSUE_DATE;

@RunWith(MockitoJUnitRunner.class)
public class SetIssueDateTest {

    private final LocalDateTime today = LocalDateTime.now();

    @InjectMocks
    private SetIssueDate setIssueDate;

    @Mock
    private Clock clock;

    @Before
    public void setup() {
        when(clock.instant()).thenReturn(today.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(UTC);
    }

    @Test
    public void testGenerateIssueDateSetsDateToNow() {
        HashMap<String, Object> payload = new HashMap<>();

        setIssueDate.execute(null, payload);

        String expectedDate = today.format(DateTimeFormatter.ofPattern(CCD_DATE_FORMAT));
        assertEquals(expectedDate, payload.get(ISSUE_DATE));
    }
}