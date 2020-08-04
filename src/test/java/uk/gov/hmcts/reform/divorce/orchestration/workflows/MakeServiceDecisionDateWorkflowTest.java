package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.MakeServiceDecisionDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.OrderToDispenseGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.MakeServiceDecisionDateWorkflow;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DISPENSED;
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

    @InjectMocks
    private MakeServiceDecisionDateWorkflow makeServiceDecisionDateWorkflow;

    @Test
    public void shouldCallOnlyMakeServiceDecisionDateTaskWhenNoApplicationServiceType() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        mockTasksExecution(caseData, makeServiceDecisionDateTask);

        makeServiceDecisionDateWorkflow.run(CaseDetails.builder().caseData(caseData).build());

        verifyTaskWasCalled(caseData, makeServiceDecisionDateTask);
        verifyTasksWereNeverCalled(orderToDispenseGenerationTask);
    }

    @Test
    public void shouldCallOnlyMakeServiceDecisionDateTaskWhenApplicationServiceTypeIsNotDispensed() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, "anything else");

        mockTasksExecution(caseData, makeServiceDecisionDateTask);

        makeServiceDecisionDateWorkflow.run(CaseDetails.builder().caseData(caseData).build());

        verifyTaskWasCalled(caseData, makeServiceDecisionDateTask);
        verifyTasksWereNeverCalled(orderToDispenseGenerationTask);
    }

    @Test
    public void shouldCallBothTasksWhenApplicationServiceTypeIsDispensed() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, DISPENSED);

        mockTasksExecution(caseData, makeServiceDecisionDateTask, orderToDispenseGenerationTask);

        makeServiceDecisionDateWorkflow.run(CaseDetails.builder().caseData(caseData).build());

        verifyTasksCalledInOrder(caseData, makeServiceDecisionDateTask, orderToDispenseGenerationTask);
    }
}
