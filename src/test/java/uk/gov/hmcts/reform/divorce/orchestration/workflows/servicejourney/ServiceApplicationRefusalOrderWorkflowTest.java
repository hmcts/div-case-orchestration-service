package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTaskTest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalDraftRemovalTask;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DEEMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DISPENSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceRefusalDecision.DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceRefusalDecision.FINAL;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class ServiceApplicationRefusalOrderWorkflowTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    @InjectMocks
    private ServiceApplicationRefusalOrderWorkflow classUnderTest;

    @Mock
    private DeemedServiceRefusalOrderTask deemedServiceRefusalOrderTask;

    @Mock
    private DispensedServiceRefusalOrderTask dispensedServiceRefusalOrderTask;

    @Mock
    private ServiceRefusalDraftRemovalTask serviceRefusalDraftRemovalTask;

    @Mock
    private DeemedServiceRefusalOrderDraftTask deemedServiceRefusalOrderDraftTask;

    @Mock
    private DispensedServiceRefusalOrderDraftTask dispensedServiceRefusalOrderDraftTask;

    public ServiceApplicationRefusalOrderWorkflowTest() {
    }

    @Test
    public void whenDeemedAndApplicationIsNotGrantedAndFinal() throws WorkflowException {
        Map<String, Object> caseData = ImmutableMap.of(
            CASE_ID_JSON_KEY, TEST_CASE_ID,
            SERVICE_APPLICATION_TYPE, DEEMED,
            SERVICE_APPLICATION_GRANTED, NO_VALUE);
        CaseDetails caseDetails =  buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        when(deemedServiceRefusalOrderTask.execute(any(), anyMap())).thenReturn(caseData);
        when(serviceRefusalDraftRemovalTask.execute(any(), anyMap())).thenReturn(caseData);

        Map<String, Object> returnedData = classUnderTest.run(caseDetails, AUTH_TOKEN, FINAL);

        assertThat(returnedData, is(notNullValue()));

        verifyTaskWasCalled(returnedData, deemedServiceRefusalOrderTask);
        verifyTaskWasCalled(returnedData, serviceRefusalDraftRemovalTask);

        verifyTasksWereNeverCalled(
            dispensedServiceRefusalOrderTask,
            deemedServiceRefusalOrderDraftTask,
            dispensedServiceRefusalOrderDraftTask);
    }

    @Test
    public void whenDispensedAndApplicationIsNotGrantedAndFinal() throws WorkflowException {
        Map<String, Object> caseData = ImmutableMap.of(
            CASE_ID_JSON_KEY, TEST_CASE_ID,
            SERVICE_APPLICATION_TYPE, DISPENSED,
            SERVICE_APPLICATION_GRANTED, NO_VALUE);
        CaseDetails caseDetails =  buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        when(dispensedServiceRefusalOrderTask.execute(any(), anyMap())).thenReturn(caseData);
        when(serviceRefusalDraftRemovalTask.execute(any(), anyMap())).thenReturn(caseData);

        Map<String, Object> returnedData = classUnderTest.run(caseDetails, AUTH_TOKEN, FINAL);

        assertThat(returnedData, is(notNullValue()));

        verifyTaskWasCalled(returnedData, dispensedServiceRefusalOrderTask);
        verifyTaskWasCalled(returnedData, serviceRefusalDraftRemovalTask);

        verifyTasksWereNeverCalled(
            deemedServiceRefusalOrderTask,
            deemedServiceRefusalOrderDraftTask,
            dispensedServiceRefusalOrderDraftTask);
    }

    @Test
    public void whenDeemedAndApplicationIsNotGrantedAndDraft() throws WorkflowException {
        Map<String, Object> caseData = ImmutableMap.of(
            CASE_ID_JSON_KEY, TEST_CASE_ID,
            SERVICE_APPLICATION_TYPE, DEEMED,
            SERVICE_APPLICATION_GRANTED, NO_VALUE);
        CaseDetails caseDetails =  buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        when(deemedServiceRefusalOrderDraftTask.execute(any(), anyMap())).thenReturn(caseData);

        Map<String, Object> returnedData = classUnderTest.run(caseDetails, AUTH_TOKEN, DRAFT);

        assertThat(returnedData, is(notNullValue()));

        verifyTaskWasCalled(returnedData, deemedServiceRefusalOrderDraftTask);

        verifyTasksWereNeverCalled(
            deemedServiceRefusalOrderTask,
            serviceRefusalDraftRemovalTask,
            dispensedServiceRefusalOrderDraftTask
        );
    }

    @Test
    public void whenDispensedAndApplicationIsNotGrantedAndDraft() throws WorkflowException {
        Map<String, Object> caseData = ImmutableMap.of(
            CASE_ID_JSON_KEY, TEST_CASE_ID,
            SERVICE_APPLICATION_TYPE, DISPENSED,
            SERVICE_APPLICATION_GRANTED, NO_VALUE);
        CaseDetails caseDetails =  buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        when(dispensedServiceRefusalOrderDraftTask.execute(any(), anyMap())).thenReturn(caseData);

        Map<String, Object> returnedData = classUnderTest.run(caseDetails, AUTH_TOKEN, DRAFT);

        assertThat(returnedData, is(notNullValue()));

        verifyTaskWasCalled(returnedData, dispensedServiceRefusalOrderDraftTask);

        verifyTasksWereNeverCalled(
            dispensedServiceRefusalOrderTask,
            serviceRefusalDraftRemovalTask,
            deemedServiceRefusalOrderDraftTask
        );
    }

    @Test
    public void whenMakeServiceDecisionAndNotAwaitingServiceConsiderationNoTasksShouldRun() throws WorkflowException {
        Map<String, Object> caseData = ImmutableMap.of(
            CASE_ID_JSON_KEY, TEST_CASE_ID,
            "anyKey", "anyValue");
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_CLARIFICATION);

        Map<String, Object> returnedData = classUnderTest.run(caseDetails, AUTH_TOKEN, FINAL);

        assertThat(returnedData, is(notNullValue()));
        runNoTasksExecutedVerification();
    }

    @Test
    public void whenMakeServiceDecisionAndServiceApplicationIsGrantedNoTasksShouldRun() throws WorkflowException {
        Map<String, Object> caseData = ImmutableMap.of(
            CASE_ID_JSON_KEY, TEST_CASE_ID,
            SERVICE_APPLICATION_GRANTED, YES_VALUE,
            "anyOtherKey", "anyOtherValue");
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        Map<String, Object> returnedData = classUnderTest.run(caseDetails, AUTH_TOKEN, FINAL);

        assertThat(returnedData, is(notNullValue()));
        runNoTasksExecutedVerification();
    }

    private CaseDetails buildCaseDetails(Map<String, Object> caseData, String caseState) {
        return CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .state(caseState)
            .build();
    }

    private void runNoTasksExecutedVerification() {
        verifyTasksWereNeverCalled(
            deemedServiceRefusalOrderTask,
            dispensedServiceRefusalOrderTask,
            serviceRefusalDraftRemovalTask,
            deemedServiceRefusalOrderDraftTask,
            dispensedServiceRefusalOrderDraftTask);
    }

}