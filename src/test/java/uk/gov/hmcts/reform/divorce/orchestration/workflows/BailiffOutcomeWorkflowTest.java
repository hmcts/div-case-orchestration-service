package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.BailiffSuccessServiceDueDateSetterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.BailiffUnsuccessServiceDueDateSetterTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.BAILIFF_SERVICE_SUCCESSFUL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.ISSUED_TO_BAILIFF;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class BailiffOutcomeWorkflowTest {

    @InjectMocks
    private BailiffOutcomeWorkflow classUnderTest;

    @Mock
    private BailiffSuccessServiceDueDateSetterTask bailiffSuccessServiceDueDateSetterTask;

    @Mock
    private BailiffUnsuccessServiceDueDateSetterTask bailiffUnsuccessServiceDueDateSetterTask;

    @Test
    public void shouldRunCorrectTask_whenBailiffServiceSuccessful() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(BAILIFF_SERVICE_SUCCESSFUL, YES_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, ISSUED_TO_BAILIFF);

        mockTasksExecution(
                caseData,
                bailiffSuccessServiceDueDateSetterTask
        );

        Map<String, Object> returnedData = executeWorkflow(caseDetails);

        verifyTasksCalledInOrder(
                returnedData,
                bailiffSuccessServiceDueDateSetterTask
        );

        verifyTasksWereNeverCalled(bailiffUnsuccessServiceDueDateSetterTask);
    }

    @Test
    public void shouldRunCorrectTask_whenBailiffServiceUnsuccessful() throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(BAILIFF_SERVICE_SUCCESSFUL, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, ISSUED_TO_BAILIFF);

        mockTasksExecution(
                caseData,
                bailiffUnsuccessServiceDueDateSetterTask
        );

        Map<String, Object> returnedData = executeWorkflow(caseDetails);

        verifyTasksCalledInOrder(
                returnedData,
                bailiffUnsuccessServiceDueDateSetterTask
        );

        verifyTasksWereNeverCalled(bailiffSuccessServiceDueDateSetterTask);
    }


    private CaseDetails buildCaseDetails(Map<String, Object> caseData, String caseState) {
        return CaseDetails.builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .state(caseState)
                .build();
    }

    private Map<String, Object> executeWorkflow(CaseDetails caseDetails)
        throws WorkflowException {
        Map<String, Object> returnedData = classUnderTest.run(caseDetails, AUTH_TOKEN);
        assertThat(returnedData, is(caseDetails.getCaseData()));

        return returnedData;
    }
}
