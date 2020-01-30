package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class AmendPetitionForRefusalTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private CreateAmendPetitionDraftForRefusalTask classUnderTest;

    @Test
    public void givenUserTokenWithoutCase_whenExecuteAmendPetition_thenReturnEmpty() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> expectedResponse = new HashMap<>();

        when(caseMaintenanceClient.amendPetitionForRefusal(AUTH_TOKEN))
            .thenReturn(null);

        assertEquals(expectedResponse, classUnderTest.execute(context, payload));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenUserTokenWithCase_whenExecuteAmendPetition_thenReturnNewDraft() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> payload = mock(Map.class);
        Map<String, Object> expectedResponse = new HashMap<>();

        when(caseMaintenanceClient.amendPetitionForRefusal(AUTH_TOKEN)).thenReturn(expectedResponse);

        assertEquals(expectedResponse, classUnderTest.execute(context, payload));
    }
}
