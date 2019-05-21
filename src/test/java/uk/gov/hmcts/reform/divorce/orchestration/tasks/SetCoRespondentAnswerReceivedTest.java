package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWER_RECEIVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWER_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@RunWith(MockitoJUnitRunner.class)
public class SetCoRespondentAnswerReceivedTest {

    private static final String CURRENT_DATE = "2018-01-01";

    @Mock
    private CcdUtil ccdUtil;

    @InjectMocks
    private SetCoRespondentAnswerReceived classToTest;

    @Before
    public void before() {
        when(ccdUtil.getCurrentDateCcdFormat()).thenReturn(CURRENT_DATE);
    }

    @Test
    public void execute() {
        Map<String, Object> caseDataResponse = classToTest.execute(null, new HashMap<>());

        Map<String, Object> expectedCaseData = ImmutableMap.of(CO_RESPONDENT_ANSWER_RECEIVED, YES_VALUE,
            CO_RESPONDENT_ANSWER_RECEIVED_DATE, CURRENT_DATE);
        assertEquals(expectedCaseData, caseDataResponse);
    }
}