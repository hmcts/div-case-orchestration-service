package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataToDivorceFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CmsCaseRetriever;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveAosCaseWorkflowTest {
    private RetrieveAosCaseWorkflow retrieveAosCaseWorkflow;

    @Mock
    private CmsCaseRetriever cmsCaseRetriever;

    @Mock
    private CaseDataToDivorceFormatter caseDataToDivorceFormatter;

    private Map<String, Object> payload;
    private TaskContext context;

    @Before
    public void setUp() throws Exception {
        retrieveAosCaseWorkflow = new RetrieveAosCaseWorkflow(cmsCaseRetriever, caseDataToDivorceFormatter);

        payload = new HashMap<>();
        payload.put("D8ScreenHasMarriageBroken", "YES");
        payload.put(PIN,TEST_PIN );


        context = new DefaultTaskContext();
    }

    @Test
    public void retrieveCaseWorkflowShouldReturnExpectedResponse() throws TaskException, WorkflowException {
        //Given
        when(cmsCaseRetriever.execute(context, new HashMap<>(), AUTH_TOKEN, true)).thenReturn(payload);
        when(caseDataToDivorceFormatter.execute(context, payload, AUTH_TOKEN, true)).thenReturn(payload);

        //When
        Map<String, Object> actual = retrieveAosCaseWorkflow.run(true, AUTH_TOKEN);

        //Then
        assertNotNull(actual);
        assertEquals(2, actual.size());
        assertTrue(actual.containsKey(PIN));

    }

    @After
    public void tearDown() throws Exception {
        retrieveAosCaseWorkflow = null;
    }
}