package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWER_RECEIVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWER_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
@Component
public class SetCoRespondentAnswerReceivedTest {

    private SetCoRespondentAnswerReceived classToTest;

    @Before
    public void setUp() {
        classToTest = new SetCoRespondentAnswerReceived();
    }

    @Test
    public void execute() {
        Map<String, Object> caseDataResponse = classToTest.execute(null, new HashMap<>());

        Map<String, Object> expectedCaseData = ImmutableMap.of(CO_RESPONDENT_ANSWER_RECEIVED, YES_VALUE,
                CO_RESPONDENT_ANSWER_RECEIVED_DATE, CcdUtil.getCurrentDate());
        assertEquals(expectedCaseData, caseDataResponse);
    }
}