package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWER_RECEIVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWER_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
@Component
public class SetCoRespondentAnswerReceivedTest {

    private static final String CURRENT_DATE = "2018-01-01";

    private SetCoRespondentAnswerReceived classToTest;

    @Before
    public void setUp() {
        classToTest = new SetCoRespondentAnswerReceived();
        DateTimeUtils.setCurrentMillisFixed(new org.joda.time.LocalDate(CURRENT_DATE).toDate().getTime());
    }

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void execute() {
        Map<String, Object> caseDataResponse = classToTest.execute(null, new HashMap<>());

        Map<String, Object> expectedCaseData = ImmutableMap.of(CO_RESPONDENT_ANSWER_RECEIVED, YES_VALUE,
                CO_RESPONDENT_ANSWER_RECEIVED_DATE, CURRENT_DATE);
        assertEquals(expectedCaseData, caseDataResponse);
    }
}