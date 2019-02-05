package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CHECK_CCD;

@RunWith(MockitoJUnitRunner.class)
public class AmendPetitionTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private CreateAmendPetitionDraft target;

    @SuppressWarnings("unchecked")
    @Test
    public void givenUserTokenWithoutCase_whenExecuteAmendPetition_thenReturnEmpty() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> payload = mock(Map.class);

        when(caseMaintenanceClient.amendPetition(AUTH_TOKEN))
                .thenReturn(null);

        assertNull(target.execute(context, payload));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenUserTokenWithCase_whenExecuteAmendPetition_thenReturnNewDraft() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        Map<String, Object> payload = mock(Map.class);
        Map<String, Object> expectedResponse = mock(Map.class);

        when(caseMaintenanceClient.amendPetition(AUTH_TOKEN)).thenReturn(expectedResponse);

        assertEquals(expectedResponse, target.execute(context, payload));
    }
}
