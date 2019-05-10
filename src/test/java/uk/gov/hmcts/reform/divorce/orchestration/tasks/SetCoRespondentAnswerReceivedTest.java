package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWER_RECEIVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWER_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@RunWith(MockitoJUnitRunner.class)
public class SetCoRespondentAnswerReceivedTest {

    private static final String CURRENT_DATE = "2018-01-01";
    private static final LocalDateTime FIXED_DATE_TIME = LocalDate.parse(CURRENT_DATE).atStartOfDay();

    @Mock
    private Clock clock;

    @InjectMocks
    private SetCoRespondentAnswerReceived classToTest;

    @Before
    public void setUp() {
        when(clock.instant()).thenReturn(FIXED_DATE_TIME.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(UTC);
    }

    @Test
    public void execute() {
        Map<String, Object> caseDataResponse = classToTest.execute(null, new HashMap<>());

        Map<String, Object> expectedCaseData = ImmutableMap.of(CO_RESPONDENT_ANSWER_RECEIVED, YES_VALUE,
            CO_RESPONDENT_ANSWER_RECEIVED_DATE, CURRENT_DATE);
        assertEquals(expectedCaseData, caseDataResponse);
    }
}