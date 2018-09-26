package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SetCaseIdAndStateOnSessionTest {

    @InjectMocks
    private SetCaseIdAndStateOnSession setCaseIdAndStateOnSession;

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = new HashMap<>();
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(CASE_STATE_JSON_KEY, TEST_STATE);
    }

    @Test
    public void executeShouldSetCaseIdAndStateOnExecute() {
        Map<String, Object> resultData = new HashMap<>();
        resultData.put(CASE_ID_JSON_KEY, TEST_CASE_ID);
        resultData.put(CASE_STATE_JSON_KEY, TEST_STATE);

        assertEquals(resultData, setCaseIdAndStateOnSession.execute(context, testData));
    }
}
