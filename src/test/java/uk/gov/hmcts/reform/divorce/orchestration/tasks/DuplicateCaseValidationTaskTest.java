package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_HWF_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_MADE_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow.SELECTED_COURT;

@RunWith(MockitoJUnitRunner.class)
public class DuplicateCaseValidationTaskTest {

    @Mock
    private TaskCommons taskCommons;

    @Mock
    private CaseMaintenanceClient mockCaseMaintenanceClient;

    @InjectMocks
    private DuplicateCaseValidationTask duplicateCaseValidationTask;

    private TaskContext taskContext;

    @Before
    public void setUp() throws TaskException {
        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        Court testCourt = new Court();
        testCourt.setCourtId(TEST_COURT);
        when(taskCommons.getCourt(TEST_COURT)).thenReturn(testCourt);
    }

    @Test
    public void givenGetCaseThrowsANotFoundException_whenExecute_thenDoNothing() throws TaskException {
        Response response = Response.builder()
            .request(Request.create(Request.HttpMethod.GET, "http//example.com", Collections.emptyMap(), null))
            .headers(Collections.emptyMap())
            .status(HttpStatus.NOT_FOUND.value()).build();

        when(mockCaseMaintenanceClient.getCase(AUTH_TOKEN))
            .thenThrow(FeignException.errorStatus("test", response));

        HashMap<String, Object> payload = new HashMap<>();
        Map<String, Object> result = duplicateCaseValidationTask.execute(taskContext, payload);

        verify(mockCaseMaintenanceClient).getCase(AUTH_TOKEN);
        assertThat(result, is(payload));
        assertThat(payload.size(), is(0));
    }

    @Test(expected = FeignException.class)
    public void givenGetCaseThrowsAServerException_whenExecute_thenThrowException() throws TaskException {
        Response response = Response.builder()
            .request(Request.create(Request.HttpMethod.GET, "http//example.com", Collections.emptyMap(), null))
            .headers(Collections.emptyMap())
            .status(HttpStatus.GATEWAY_TIMEOUT.value()).build();

        when(mockCaseMaintenanceClient.getCase(AUTH_TOKEN))
            .thenThrow(FeignException.errorStatus("test", response));

        HashMap<String, Object> payload = new HashMap<>();
        Map<String, Object> result = duplicateCaseValidationTask.execute(taskContext, payload);

        verify(mockCaseMaintenanceClient).getCase(AUTH_TOKEN);
        assertThat(result, is(payload));
        assertThat(payload.size(), is(0));
    }

    @Test
    public void givenNoExistingCaseForUser_whenExecute_thenDoNothing() throws TaskException {
        when(mockCaseMaintenanceClient.getCase(AUTH_TOKEN))
            .thenReturn(null);

        HashMap<String, Object> payload = new HashMap<>();
        Map<String, Object> result = duplicateCaseValidationTask.execute(taskContext, payload);

        verify(mockCaseMaintenanceClient).getCase(AUTH_TOKEN);
        assertThat(result, is(payload));
        assertThat(payload.size(), is(0));
    }

    @Test
    public void givenCaseInAwaitingPayment_whenExecute_thenSetFieldsInPayload() throws TaskException {
        when(mockCaseMaintenanceClient.getCase(AUTH_TOKEN))
            .thenReturn(
                CaseDetails.builder()
                    .caseId(TEST_CASE_ID)
                    .state(AWAITING_PAYMENT)
                    .caseData(Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT))
                    .build()
            );

        HashMap<String, Object> payload = new HashMap<>();
        Map<String, Object> result = duplicateCaseValidationTask.execute(taskContext, payload);

        verify(mockCaseMaintenanceClient).getCase(AUTH_TOKEN);
        assertThat(result, is(payload));
        assertThat(payload.get(ID), is(TEST_CASE_ID));
        Court selectedCourt = taskContext.getTransientObject(SELECTED_COURT);
        assertThat(selectedCourt.getCourtId(), is(TEST_COURT));
    }

    @Test
    public void givenCaseInAwaitingHelpWithFees_whenExecute_thenSetFieldsInPayload() throws TaskException {
        when(mockCaseMaintenanceClient.getCase(AUTH_TOKEN))
            .thenReturn(
                CaseDetails.builder()
                    .caseId(TEST_CASE_ID)
                    .state(AWAITING_HWF_DECISION)
                    .caseData(Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT))
                    .build()
            );

        HashMap<String, Object> payload = new HashMap<>();
        Map<String, Object> result = duplicateCaseValidationTask.execute(taskContext, payload);

        verify(mockCaseMaintenanceClient).getCase(AUTH_TOKEN);
        assertThat(result, is(payload));
        assertThat(payload.get(ID), is(TEST_CASE_ID));
        Court selectedCourt = taskContext.getTransientObject(SELECTED_COURT);
        assertThat(selectedCourt.getCourtId(), is(TEST_COURT));
    }

    @Test
    public void givenCaseNotInAwaitingPayment_whenExecute_thenDoNothing() throws TaskException {
        when(mockCaseMaintenanceClient.getCase(AUTH_TOKEN))
            .thenReturn(
                CaseDetails.builder()
                    .caseId(TEST_CASE_ID)
                    .state(PAYMENT_MADE_EVENT)
                    .caseData(Collections.singletonMap(D_8_DIVORCE_UNIT, TEST_COURT))
                    .build()
            );

        HashMap<String, Object> payload = new HashMap<>();
        Map<String, Object> result = duplicateCaseValidationTask.execute(taskContext, payload);

        verify(mockCaseMaintenanceClient).getCase(AUTH_TOKEN);
        assertThat(result, is(payload));
        assertThat(payload.size(), is(0));
    }
}