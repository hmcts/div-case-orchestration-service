package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bailiff.BailiffApplicationApprovedDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.MakeServiceDecisionDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.OrderToDispenseGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceApplicationDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceApplicationRemovalTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalDraftRemovalTask;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class MakeServiceDecisionWorkflowTest extends TestCase {

    @Mock private MakeServiceDecisionDateTask makeServiceDecisionDateTask;
    @Mock private OrderToDispenseGenerationTask orderToDispenseGenerationTask;
    @Mock private DeemedServiceOrderGenerationTask deemedServiceOrderGenerationTask;
    @Mock private DeemedServiceRefusalOrderTask deemedServiceRefusalOrderTask;
    @Mock private DispensedServiceRefusalOrderTask dispensedServiceRefusalOrderTask;
    @Mock private ServiceRefusalDraftRemovalTask serviceRefusalDraftRemovalTask;
    @Mock private ServiceApplicationDataTask serviceApplicationDataTask;
    @Mock private ServiceApplicationRemovalTask serviceApplicationRemovalTask;
    @Mock private BailiffApplicationApprovedDataTask bailiffApplicationApprovedDataTask;

    @InjectMocks
    private MakeServiceDecisionWorkflow makeServiceDecisionWorkflow;

    @Test
    public void shouldCallOnlyMakeServiceDecisionDateTaskWhenNoApplicationServiceType() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        runExpectingDecisionDateServiceApplicationDataAndDraftRemovalWillBeCalled(caseData);
    }

    @Test
    public void shouldCallOnlyMakeServiceDecisionDateTaskWhenApplicationServiceIsNotGranted() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, NO_VALUE);

        runExpectingDecisionDateServiceApplicationDataAndDraftRemovalWillBeCalled(caseData);
    }

    @Test
    public void shouldCallOnlyMakeServiceDecisionDateTaskWhenApplicationServiceTypeIsNotDispensed() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, YES_VALUE);
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, "anything else");

        runExpectingOnlyDecisionDateAndServiceApplicationDataTasksWillBeCalled(caseData);
    }

    @Test
    public void shouldCallTwoTasksWhenApplicationServiceTypeIsDispensed() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, YES_VALUE);
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.DISPENSED);

        mockTasksExecution(
            caseData,
            makeServiceDecisionDateTask,
            orderToDispenseGenerationTask,
            serviceApplicationDataTask,
            serviceApplicationRemovalTask
        );

        makeServiceDecisionWorkflow.run(CaseDetails.builder().caseData(caseData).build(), AUTH_TOKEN);

        verifyTasksCalledInOrder(caseData, makeServiceDecisionDateTask, orderToDispenseGenerationTask);
        verifyTasksWereNeverCalled(deemedServiceOrderGenerationTask);
    }

    @Test
    public void shouldCallTwoTasksWhenApplicationServiceTypeIsDeemedAndNotGranted() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, NO_VALUE);
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.DEEMED);

        mockTasksExecution(
            caseData,
            makeServiceDecisionDateTask,
            deemedServiceRefusalOrderTask,
            serviceRefusalDraftRemovalTask,
            serviceApplicationDataTask,
            serviceApplicationRemovalTask
        );

        makeServiceDecisionWorkflow.run(CaseDetails.builder().caseData(caseData).build(), AUTH_TOKEN);

        verifyTasksCalledInOrder(
            caseData,
            makeServiceDecisionDateTask,
            deemedServiceRefusalOrderTask,
            serviceRefusalDraftRemovalTask,
            serviceApplicationDataTask,
            serviceApplicationRemovalTask
        );
        verifyTasksWereNeverCalled(deemedServiceOrderGenerationTask);
    }

    @Test
    public void shouldCallTwoTasksWhenApplicationServiceTypeIsDispensedAndNotGranted() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, NO_VALUE);
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.DISPENSED);

        mockTasksExecution(
            caseData,
            makeServiceDecisionDateTask,
            dispensedServiceRefusalOrderTask,
            serviceRefusalDraftRemovalTask,
            serviceApplicationDataTask,
            serviceApplicationRemovalTask
        );

        makeServiceDecisionWorkflow.run(CaseDetails.builder().caseData(caseData).build(), AUTH_TOKEN);

        verifyTasksCalledInOrder(
            caseData,
            makeServiceDecisionDateTask,
            dispensedServiceRefusalOrderTask,
            serviceRefusalDraftRemovalTask,
            serviceApplicationDataTask,
            serviceApplicationRemovalTask
        );
        verifyTasksWereNeverCalled(deemedServiceOrderGenerationTask);
    }

    @Test
    public void shouldCallTwoTasksWhenApplicationServiceTypeIsDeemed() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, YES_VALUE);
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.DEEMED);

        mockTasksExecution(
            caseData,
            makeServiceDecisionDateTask,
            deemedServiceOrderGenerationTask,
            serviceApplicationDataTask,
            serviceApplicationRemovalTask
        );

        makeServiceDecisionWorkflow.run(CaseDetails.builder().caseData(caseData).build(), AUTH_TOKEN);

        verifyTasksCalledInOrder(
            caseData,
            makeServiceDecisionDateTask,
            deemedServiceOrderGenerationTask,
            serviceApplicationDataTask,
            serviceApplicationRemovalTask
        );
        verifyTasksWereNeverCalled(orderToDispenseGenerationTask, serviceRefusalDraftRemovalTask);
    }

    @Test
    public void shouldNotMoveServiceApplicationDataToCollection_whenBailiffApplicationGranted() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, YES_VALUE);
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.BAILIFF);

        mockTasksExecution(
            caseData,
            makeServiceDecisionDateTask,
            bailiffApplicationApprovedDataTask
        );

        makeServiceDecisionWorkflow.run(CaseDetails.builder().caseData(caseData).build(), AUTH_TOKEN);

        verifyTasksCalledInOrder(
            caseData,
            makeServiceDecisionDateTask,
            bailiffApplicationApprovedDataTask
        );
        verifyTasksWereNeverCalled(
            orderToDispenseGenerationTask,
            deemedServiceOrderGenerationTask,
            dispensedServiceRefusalOrderTask,
            deemedServiceRefusalOrderTask,
            serviceRefusalDraftRemovalTask
        );
    }

    private void runExpectingDecisionDateServiceApplicationDataAndDraftRemovalWillBeCalled(Map<String, Object> caseData)
        throws WorkflowException {
        mockTasksExecution(caseData,
            makeServiceDecisionDateTask,
            serviceRefusalDraftRemovalTask,
            serviceApplicationDataTask,
            serviceApplicationRemovalTask
        );

        makeServiceDecisionWorkflow.run(CaseDetails.builder().caseData(caseData).build(), AUTH_TOKEN);

        verifyTasksCalledInOrder(
            caseData,
            makeServiceDecisionDateTask,
            serviceRefusalDraftRemovalTask,
            serviceApplicationDataTask,
            serviceApplicationRemovalTask
        );

        verifyTasksWereNeverCalled(
            orderToDispenseGenerationTask,
            deemedServiceOrderGenerationTask,
            deemedServiceRefusalOrderTask,
            dispensedServiceRefusalOrderTask
        );
    }

    private void runExpectingOnlyDecisionDateAndServiceApplicationDataTasksWillBeCalled(Map<String, Object> caseData)
        throws WorkflowException {
        mockTasksExecution(caseData,
            makeServiceDecisionDateTask,
            serviceApplicationDataTask,
            serviceApplicationRemovalTask
        );

        makeServiceDecisionWorkflow.run(CaseDetails.builder().caseData(caseData).build(), AUTH_TOKEN);

        verifyTasksCalledInOrder(
            caseData,
            makeServiceDecisionDateTask,
            serviceApplicationDataTask,
            serviceApplicationRemovalTask
        );

        verifyTasksWereNeverCalled(
            orderToDispenseGenerationTask,
            deemedServiceOrderGenerationTask,
            deemedServiceRefusalOrderTask,
            serviceRefusalDraftRemovalTask,
            dispensedServiceRefusalOrderTask
        );
    }
}
