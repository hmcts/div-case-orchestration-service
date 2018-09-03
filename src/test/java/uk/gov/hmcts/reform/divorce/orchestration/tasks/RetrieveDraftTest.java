package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveDraftTest {
    private static final String CASE_DATA_KEY = "case_data";

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private RetrieveDraft target;

    @Test
    public void givenUserTokenWithoutDraft_whenExecuteRetrieveTask_thenReturnEmptyResponse() {
        TaskContext context = mock(TaskContext.class);
        Map<String, Object> payload  = mock(Map.class);
        Map<String, Object> emptyResponse  = mock(Map.class);

        when(caseMaintenanceClient.retrievePetition(AUTH_TOKEN, true)).thenReturn(emptyResponse);

        assertTrue(target.execute(context, payload, AUTH_TOKEN).isEmpty());
    }

    @Test
    public void givenUserToken_whenExecuteRetrieveTask_thenReturnUserPetitionFromCMS() {
        TaskContext context = mock(TaskContext.class);
        Map<String, Object> payload  = mock(Map.class);
        Map<String, Object> expectedResponse  = mock(Map.class);
        Map<String, Object> clientResponse  = new LinkedHashMap<>();
        clientResponse.put(CASE_DATA_KEY, expectedResponse);

        when(caseMaintenanceClient.retrievePetition(AUTH_TOKEN, true)).thenReturn(clientResponse);

        assertEquals(expectedResponse,target.execute(context, payload, AUTH_TOKEN));
    }
}
