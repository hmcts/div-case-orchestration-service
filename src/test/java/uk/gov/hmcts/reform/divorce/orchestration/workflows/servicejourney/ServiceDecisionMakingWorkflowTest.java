package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderDraftTask;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DEEMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DISPENSED;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class ServiceDecisionMakingWorkflowTest {

    @InjectMocks
    private ServiceDecisionMakingWorkflow classUnderTest;

    @Mock
    private DeemedServiceRefusalOrderDraftTask deemedServiceRefusalOrderDraftTask;

    @Mock
    private DispensedServiceRefusalOrderDraftTask dispensedServiceRefusalOrderDraftTask;

    @Test
    public void whenDeemedAndApplicationIsNotGranted() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DEEMED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        mockTasksExecution(caseData, deemedServiceRefusalOrderDraftTask);

        Map<String, Object> returnedData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedData, deemedServiceRefusalOrderDraftTask);
        verifyTasksWereNeverCalled(dispensedServiceRefusalOrderDraftTask);
    }

    @Test
    public void whenDispensedAndApplicationIsNotGrantedAndDraft() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DISPENSED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        mockTasksExecution(caseData, dispensedServiceRefusalOrderDraftTask);

        Map<String, Object> returnedData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedData, dispensedServiceRefusalOrderDraftTask);
        verifyTasksWereNeverCalled(deemedServiceRefusalOrderDraftTask);
    }

    @Test
    public void whenUnknownServiceApplicationTypeThenNoDraftIsGenerated() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData("other", NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        executeWorkflow(caseDetails);

        runNoTasksToGenerateDraftPdfs();
    }

    @Test
    public void whenWrongStateThenNoDraftIsGenerated() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DEEMED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, "wrongState!");

        executeWorkflow(caseDetails);

        runNoTasksToGenerateDraftPdfs();
    }

    @Test
    public void whenApplicationGrantedThenNoDraftIsGenerated() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DISPENSED, YES_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        executeWorkflow(caseDetails);

        runNoTasksToGenerateDraftPdfs();
    }

    private Map<String, Object> buildCaseData(String serviceApplicationType, String serviceApplicationGranted) {
        return ImmutableMap.of(
            SERVICE_APPLICATION_TYPE, serviceApplicationType,
            SERVICE_APPLICATION_GRANTED, serviceApplicationGranted
        );
    }

    private CaseDetails buildCaseDetails(Map<String, Object> caseData, String caseState) {
        return CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .state(caseState)
            .build();
    }

    private void runNoTasksToGenerateDraftPdfs() {
        verifyTasksWereNeverCalled(
            deemedServiceRefusalOrderDraftTask,
            dispensedServiceRefusalOrderDraftTask
        );
    }

    private Map<String, Object> executeWorkflow(CaseDetails caseDetails)
        throws WorkflowException {
        Map<String, Object> returnedData = classUnderTest.run(caseDetails, AUTH_TOKEN);
        assertThat(returnedData, is(notNullValue()));

        return returnedData;
    }
}
