package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_HWF_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_MADE_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow.SELECTED_COURT;

@RunWith(MockitoJUnitRunner.class)
public class DuplicateCaseValidationTaskTest {

    private CaseMaintenanceClient mockCaseMaintenanceClient;
    private DuplicateCaseValidationTask duplicateCaseValidationTask;

    private TaskContext taskContext;

    @Before
    public void setUp() {
        mockCaseMaintenanceClient = mock(CaseMaintenanceClient.class);
        duplicateCaseValidationTask = new DuplicateCaseValidationTask(mockCaseMaintenanceClient);
        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

    }

    @Test
    public void givenGetCaseThrowsANotFoundException_whenExecute_thenDoNothing() {
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
    public void givenGetCaseThrowsAServerException_whenExecute_thenThrowException() {
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
    public void givenNoExistingCaseForUser_whenExecute_thenDoNothing() {
        when(mockCaseMaintenanceClient.getCase(AUTH_TOKEN))
                .thenReturn(null);

        HashMap<String, Object> payload = new HashMap<>();
        Map<String, Object> result = duplicateCaseValidationTask.execute(taskContext, payload);

        verify(mockCaseMaintenanceClient).getCase(AUTH_TOKEN);
        assertThat(result, is(payload));
        assertThat(payload.size(), is(0));
    }

    @Test
    public void givenCaseInAwaitingPayment_whenExecute_thenSetFieldsInPayload() {
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
        assertThat(taskContext.getTransientObject(SELECTED_COURT), is(TEST_COURT));
    }

    @Test
    public void givenCaseInAwaitingHelpWithFees_whenExecute_thenSetFieldsInPayload() {
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
        assertThat(taskContext.getTransientObject(SELECTED_COURT), is(TEST_COURT));
    }

    @Test
    public void givenCaseNotInAwaitingPayment_whenExecute_thenDoNothing() {
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