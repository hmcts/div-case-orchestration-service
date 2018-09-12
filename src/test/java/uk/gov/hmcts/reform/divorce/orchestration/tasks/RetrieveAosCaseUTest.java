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
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CHECK_CCD;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CHECK_CCD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveAosCaseUTest {
    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private RetrieveAosCase classUnderTest;

    @Test
    public void givenNoCaseExists_whenRetrieveAosCase_thenReturnEmptyResponse() {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CHECK_CCD, TEST_CHECK_CCD);

        Mockito.when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN, TEST_CHECK_CCD)).thenReturn(null);

        CaseDataResponse actual = classUnderTest.execute(context, null);

        assertNull(actual.getCaseId());
        assertNull(actual.getCourts());
        assertNull(actual.getState());
        assertNull(actual.getData());
        assertTrue(context.getStatus());

        Mockito.verify(caseMaintenanceClient).retrieveAosCase(AUTH_TOKEN, TEST_CHECK_CCD);
    }

    @Test
    public void givenCaseExists_whenRetrieveAosCase_thenReturnExpectedOutput() {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CHECK_CCD, TEST_CHECK_CCD);

        final Map<String, Object> caseData = Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT);

        final CaseDetails cmsResponse =
            CaseDetails.builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .build();

        Mockito.when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN, TEST_CHECK_CCD)).thenReturn(cmsResponse);

        CaseDataResponse actual = classUnderTest.execute(context, null);

        assertEquals(TEST_CASE_ID, actual.getCaseId());
        assertEquals(TEST_COURT, actual.getCourts());
        assertEquals(TEST_STATE, actual.getState());
        assertEquals(caseData, context.getTransientObject(CCD_CASE_DATA));

        Mockito.verify(caseMaintenanceClient).retrieveAosCase(AUTH_TOKEN, TEST_CHECK_CCD);
    }
}