package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;

@RunWith(MockitoJUnitRunner.class)
public class GetCaseUTest {
    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private GetCase classUnderTest;

    @Test
    public void givenNoCaseExists_whenGetCase_thenReturnThrowException() {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        Mockito.when(caseMaintenanceClient.getCase(AUTH_TOKEN)).thenReturn(null);

        try {
            classUnderTest.execute(context, null);
        } catch (TaskException taskException) {
            assertTrue(taskException.getCause() instanceof CaseNotFoundException);
        }

        verify(caseMaintenanceClient).getCase(AUTH_TOKEN);
    }

    @Test
    public void givenCaseExists_whenGetCase_thenReturnExpectedOutput() throws TaskException {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        final Map<String, Object> caseData = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);

        final CaseDetails cmsResponse =
            CaseDetails.builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .build();

        Mockito.when(caseMaintenanceClient.getCase(AUTH_TOKEN)).thenReturn(cmsResponse);

        CaseDataResponse actual = classUnderTest.execute(context, null);

        assertEquals(TEST_CASE_ID, actual.getCaseId());
        assertEquals(TEST_COURT, actual.getCourt());
        assertEquals(TEST_STATE, actual.getState());
        assertEquals(caseData, context.getTransientObject(CCD_CASE_DATA));

        verify(caseMaintenanceClient).getCase(AUTH_TOKEN);
    }
}