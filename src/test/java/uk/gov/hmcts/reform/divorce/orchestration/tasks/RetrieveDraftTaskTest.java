package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveDraftTaskTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private RetrieveDraftTask retrieveDraftTask;

    @SuppressWarnings("unchecked")
    @Test
    public void givenUserTokenWithoutDraft_whenExecuteRetrieveTask_thenReturnEmptyResponse() {
        TaskContext context = contextWithToken();

        Map<String, Object> payload = new HashMap<>();

        when(caseMaintenanceClient.retrievePetition(AUTH_TOKEN))
            .thenReturn(CaseDetails.builder().build());

        assertNull(retrieveDraftTask.execute(context, payload));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenUserToken_whenExecuteRetrieveTask_thenReturnUserPetitionFromCMS() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> payload = mock(Map.class);
        Map<String, Object> expectedResponse = mock(Map.class);
        CaseDetails clientResponse = CaseDetails.builder()
            .caseData(expectedResponse)
            .build();

        when(caseMaintenanceClient.retrievePetition(AUTH_TOKEN)).thenReturn(clientResponse);

        assertEquals(expectedResponse, retrieveDraftTask.execute(context, payload));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseExists_whenExecuteRetrieveTask_thenReturnUserPetitionFromCMSWithCaseDetails() {
        TaskContext context = contextWithToken();

        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> expectedResponse = ImmutableMap.of("field", "populated");

        CaseDetails clientResponse = CaseDetails.builder()
            .caseData(expectedResponse)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();

        when(caseMaintenanceClient.retrievePetition(AUTH_TOKEN)).thenReturn(clientResponse);

        assertEquals(expectedResponse, retrieveDraftTask.execute(context, payload));
        assertEquals(TEST_CASE_ID, context.getTransientObject(CASE_ID_JSON_KEY));
        assertEquals(TEST_STATE, context.getTransientObject(CASE_STATE_JSON_KEY));
    }
}
