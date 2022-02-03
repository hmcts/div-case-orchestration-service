package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class CreateAmendPetitionForRefusalFromCaseIdTaskTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private CreateAmendPetitionDraftForRefusalFromCaseIdTask classUnderTest;

    @Test
    public void givenUserTokenWithoutCase_whenExecuteAmendPetitionForRefusalFromCaseId_thenReturnEmpty() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> expectedResponse = null;

        when(caseMaintenanceClient.amendPetitionForRefusalFromCaseId(AUTH_TOKEN, TEST_CASE_ID))
            .thenReturn(null);

        assertEquals(expectedResponse, classUnderTest.execute(context, payload));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenUserTokenWithCase_whenExecuteAmendPetitionForRefusalFromCaseId_thenReturnNewDraft() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        Map<String, Object> payload = mock(Map.class);
        Map<String, Object> expectedResponse = ImmutableMap.of(PREVIOUS_CASE_ID_JSON_KEY, TEST_CASE_ID);

        when(caseMaintenanceClient.amendPetitionForRefusalFromCaseId(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(expectedResponse);

        assertEquals(expectedResponse, classUnderTest.execute(context, payload));
    }
}
