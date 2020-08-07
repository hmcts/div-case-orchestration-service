package uk.gov.hmcts.reform.divorce.orchestration.workflows;

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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.MakeServiceDecisionDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.OrderToDispenseGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.MakeServiceDecisionDateWorkflow;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class MakeServiceDecisionDateWorkflowTest extends TestCase {

    @Mock
    private MakeServiceDecisionDateTask makeServiceDecisionDateTask;

    @Mock
    private OrderToDispenseGenerationTask orderToDispenseGenerationTask;

    @Mock
    private DeemedServiceOrderGenerationTask deemedServiceOrderGenerationTask;

    @InjectMocks
    private MakeServiceDecisionDateWorkflow makeServiceDecisionDateWorkflow;

    @Test
    public void shouldCallOnlyMakeServiceDecisionDateTaskWhenNoApplicationServiceType() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        runWorkflowTestForCaseDataExpectingOnlyDecisionDateTaskWillBeCalled(caseData);
    }

    @Test
    public void shouldCallOnlyMakeServiceDecisionDateTaskWhenApplicationServiceIsNotGranted() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, NO_VALUE);

        runWorkflowTestForCaseDataExpectingOnlyDecisionDateTaskWillBeCalled(caseData);
    }

    @Test
    public void shouldCallOnlyMakeServiceDecisionDateTaskWhenApplicationServiceTypeIsNotDispensed() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, YES_VALUE);
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, "anything else");

        runWorkflowTestForCaseDataExpectingOnlyDecisionDateTaskWillBeCalled(caseData);
    }

    @Test
    public void shouldTwoTasksWhenApplicationServiceTypeIsDispensed() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, YES_VALUE);
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.DISPENSED);

        mockTasksExecution(caseData, makeServiceDecisionDateTask, orderToDispenseGenerationTask);

        makeServiceDecisionDateWorkflow.run(CaseDetails.builder().caseData(caseData).build(), AUTH_TOKEN);

        verifyTasksCalledInOrder(caseData, makeServiceDecisionDateTask, orderToDispenseGenerationTask);
        verifyTasksWereNeverCalled(deemedServiceOrderGenerationTask);
    }

    @Test
    public void shouldCallTwoTasksWhenApplicationServiceTypeIsDeemed() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, YES_VALUE);
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.DEEMED);

        mockTasksExecution(caseData, makeServiceDecisionDateTask, deemedServiceOrderGenerationTask);

        makeServiceDecisionDateWorkflow.run(CaseDetails.builder().caseData(caseData).build(), AUTH_TOKEN);

        verifyTasksCalledInOrder(caseData, makeServiceDecisionDateTask, deemedServiceOrderGenerationTask);
        verifyTasksWereNeverCalled(orderToDispenseGenerationTask);
    }

    private void runWorkflowTestForCaseDataExpectingOnlyDecisionDateTaskWillBeCalled(Map<String, Object> caseData)
        throws WorkflowException {
        mockTasksExecution(caseData, makeServiceDecisionDateTask);

        makeServiceDecisionDateWorkflow.run(CaseDetails.builder().caseData(caseData).build(), AUTH_TOKEN);

        verifyTaskWasCalled(caseData, makeServiceDecisionDateTask);
        verifyTasksWereNeverCalled(orderToDispenseGenerationTask);
        verifyTasksWereNeverCalled(deemedServiceOrderGenerationTask);
    }
}
