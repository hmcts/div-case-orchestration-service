package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateBulkCaseInCcd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_DATA_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class UpdateBulkCaseWorkflowTest {

    @Mock
    UpdateBulkCaseInCcd updateBulkCaseInCcd;

    @InjectMocks
    UpdateBulkCaseWorkflow updateBulkCaseWorkflow;

    private Map<String, Object> eventData;
    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        eventData = new HashMap<>();
        testData = Collections.emptyMap();
        eventData.put(CASE_EVENT_DATA_JSON_KEY, testData);
        eventData.put(CASE_EVENT_ID_JSON_KEY, TEST_EVENT_ID);

        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(CASE_EVENT_ID_JSON_KEY, TEST_EVENT_ID);
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        when(updateBulkCaseInCcd.execute(context, testData)).thenReturn(testData);

        assertEquals(testData, updateBulkCaseWorkflow.run(testData, AUTH_TOKEN, TEST_CASE_ID, TEST_EVENT_ID));

        verify(updateBulkCaseInCcd).execute(context, testData);
    }
}
