package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class SaveToDraftStoreTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private SaveToDraftStore target;

    @Test
    public void givenUserToken_whenExecuteSaveDraftTask_thenDraftIsSentToCMS() {
        TaskContext context = mock(TaskContext.class);
        Map<String, Object> payload  = mock(Map.class);
        Map<String, Object> expectedResponse  = mock(Map.class);
        final boolean divorceFormat = true;

        when(caseMaintenanceClient.saveDraft(payload, AUTH_TOKEN, divorceFormat)).thenReturn(expectedResponse);

        Assert.assertEquals(expectedResponse, target.execute(context, payload, AUTH_TOKEN, TEST_USER_EMAIL,
                divorceFormat));

        verify(caseMaintenanceClient).saveDraft(payload, AUTH_TOKEN, divorceFormat);
    }

    @Test
    public void givenUserTokenWithDivorceFormatFalse_whenExecuteSaveDraftTask_thenDraftIsSentToCMS() {
        TaskContext context = mock(TaskContext.class);
        Map<String, Object> payload  = mock(Map.class);
        Map<String, Object> expectedResponse  = mock(Map.class);
        final boolean divorceFormat = false;

        when(caseMaintenanceClient.saveDraft(payload, AUTH_TOKEN, divorceFormat)).thenReturn(expectedResponse);

        Assert.assertEquals(expectedResponse, target.execute(context, payload, AUTH_TOKEN, TEST_USER_EMAIL,
                divorceFormat));

        verify(caseMaintenanceClient).saveDraft(payload, AUTH_TOKEN, divorceFormat);
    }
}
