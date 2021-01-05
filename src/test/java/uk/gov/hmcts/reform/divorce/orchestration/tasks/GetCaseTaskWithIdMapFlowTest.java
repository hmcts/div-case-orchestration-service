package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.Collections;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;

@RunWith(MockitoJUnitRunner.class)
public class GetCaseTaskWithIdMapFlowTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private GetCaseWithIdMapFlowTask classUnderTest;

    @Test
    public void givenNoCaseExists_whenGetCase_thenThrowTaskException() throws TaskException {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        Mockito.when(authUtil.getCaseworkerToken()).thenReturn(CASEWORKER_AUTH_TOKEN);
        Mockito.when(caseMaintenanceClient.retrievePetitionById(CASEWORKER_AUTH_TOKEN, TEST_CASE_ID))
            .thenReturn(null);
        final String exceptionMessage = String.format("No case found with ID [%s]", TEST_CASE_ID);
        Map<String, Object> payload = emptyMap();

        TaskException taskException = assertThrows(
            TaskException.class,
            () -> classUnderTest.execute(context, payload)
        );

        assertThat(taskException.getCause(), is(instanceOf(CaseNotFoundException.class)));
        assertThat(taskException.getMessage(), endsWith(exceptionMessage));

        verify(caseMaintenanceClient).retrievePetitionById(CASEWORKER_AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void givenCaseExists_whenGetCase_thenReturnExpectedOutput() throws TaskException {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        final CaseDetails cmsResponse =
            CaseDetails.builder()
                .caseData(Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT))
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .build();

        Mockito.when(caseMaintenanceClient.retrievePetitionById(CASEWORKER_AUTH_TOKEN, TEST_CASE_ID))
            .thenReturn(cmsResponse);
        Mockito.when(authUtil.getCaseworkerToken()).thenReturn(CASEWORKER_AUTH_TOKEN);

        classUnderTest.execute(context, emptyMap());

        assertEquals(cmsResponse, context.getTransientObject(CASE_DETAILS_JSON_KEY));

        verify(caseMaintenanceClient).retrievePetitionById(CASEWORKER_AUTH_TOKEN, TEST_CASE_ID);
    }
}
