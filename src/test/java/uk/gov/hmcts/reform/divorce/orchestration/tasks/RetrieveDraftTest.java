package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveDraftTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private RetrieveDraft target;

    @Test
    public void givenUserToken_whenExecuteRetrieveTask_thenReturnUserPetitionFromCMS() {
        TaskContext context = mock(TaskContext.class);
        Map<String, Object> payload  = mock(Map.class);
        Map<String, Object> expectedResponse  = mock(Map.class);

        when(caseMaintenanceClient.retrievePetition(AUTH_TOKEN)).thenReturn(expectedResponse);

        assertEquals(expectedResponse,target.execute(context, payload, AUTH_TOKEN));
    }
}
