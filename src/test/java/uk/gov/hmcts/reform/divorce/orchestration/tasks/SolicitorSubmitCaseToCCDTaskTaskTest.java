package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorSubmitCaseToCCDTaskTaskTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private SolicitorSubmitCaseToCCDTask solicitorSubmitCaseToCCD;

    @Test
    public void executeShouldCallCaseMaintenanceClientSolicitorSubmitEndpoint() {
        final Map<String, Object> testData = Collections.emptyMap();
        final TaskContext context = new DefaultTaskContext();

        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        Map<String, Object> resultData = Collections.singletonMap("Hello", "World");

        when(caseMaintenanceClient.solicitorSubmitCase(testData, AUTH_TOKEN)).thenReturn(resultData);

        assertEquals(resultData, solicitorSubmitCaseToCCD.execute(context, testData));

        verify(caseMaintenanceClient).solicitorSubmitCase(testData, AUTH_TOKEN);
    }
}
