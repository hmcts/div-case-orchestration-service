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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CHECK_CCD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;

@RunWith(MockitoJUnitRunner.class)
public class RetrieveAosCaseUTest {
    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private RetrieveAosCase classUnderTest;

    @Test
    public void whenRetrieveAosCase_thenReturnExpectedOutput() {
        final String authToken = "auth token";
        final boolean checkCcd = true;
        final String court = "court";
        final String caseId = "caseId";
        final String state = "state";

        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, authToken);
        context.setTransientObject(CHECK_CCD, checkCcd);

        final Map<String, Object> caseData = Collections.singletonMap(D_8_DIVORCE_UNIT, court);

        final CaseDetails cmsResponse =
            CaseDetails.builder()
                .caseData(caseData)
                .caseId(caseId)
                .state(state)
                .build();

        Mockito.when(caseMaintenanceClient.retrieveAosCase(authToken, checkCcd)).thenReturn(cmsResponse);

        CaseDataResponse actual = classUnderTest.execute(context, null);

        assertEquals(caseId, actual.getCaseId());
        assertEquals(court, actual.getCourts());
        assertEquals(state, actual.getState());
        assertEquals(caseData, actual.getData());

        Mockito.verify(caseMaintenanceClient).retrieveAosCase(authToken, checkCcd);
    }
}