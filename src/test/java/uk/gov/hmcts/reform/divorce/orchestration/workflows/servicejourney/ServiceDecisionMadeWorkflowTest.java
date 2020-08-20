package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedNotApprovedEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedApprovedEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedNotApprovedEmailTask;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DEEMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DISPENSED;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasNeverCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class ServiceDecisionMadeWorkflowTest {

    @InjectMocks
    private ServiceDecisionMadeWorkflow classUnderTest;

    @Mock
    private DeemedApprovedEmailTask deemedApprovedEmailTask;

    @Mock
    private DeemedNotApprovedEmailTask deemedNotApprovedEmailTask;

    @Mock
    private DispensedApprovedEmailTask dispensedApprovedEmailTask;

    @Mock
    private DispensedNotApprovedEmailTask dispensedNotApprovedEmailTask;

    @Test
    public void whenDeemedAndApplicationIsNotGranted() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DEEMED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, SERVICE_APPLICATION_NOT_APPROVED);

        mockTasksExecution(
            caseData,
            deemedNotApprovedEmailTask
        );

        Map<String, Object> returnedData = executeWorkflow(caseDetails);

        verifyTasksCalledInOrder(
            returnedData,
            deemedNotApprovedEmailTask
        );

        runNoTasksToSendApprovedEmails();
        verifyTasksWereNeverCalled(dispensedNotApprovedEmailTask);
    }

    @Test
    public void whenDispensedAndApplicationIsNotGranted() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DISPENSED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, SERVICE_APPLICATION_NOT_APPROVED);

        mockTasksExecution(
            caseData,
            dispensedNotApprovedEmailTask
        );

        Map<String, Object> returnedData = executeWorkflow(caseDetails);

        verifyTasksCalledInOrder(
            returnedData,
            dispensedNotApprovedEmailTask
        );

        runNoTasksToSendApprovedEmails();
        verifyTasksWereNeverCalled(deemedNotApprovedEmailTask);
    }

    @Test
    public void whenServiceDecisionMadeAndServiceApplicationIsGrantedAndDeemedShouldSendEmail() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DEEMED, YES_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        mockTasksExecution(caseData, deemedApprovedEmailTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, deemedApprovedEmailTask);

        verifyTaskWasNeverCalled(dispensedApprovedEmailTask);
        runNoTasksToSendNotApprovedEmails();
    }

    @Test
    public void whenApplicationIsGrantedAndDispensedShouldSendDispensedApprovedEmail()
        throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DISPENSED, YES_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        mockTasksExecution(caseData, dispensedApprovedEmailTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, dispensedApprovedEmailTask);

        verifyTaskWasNeverCalled(deemedApprovedEmailTask);
        runNoTasksToSendNotApprovedEmails();
    }

    @Test
    public void whenApplicationIsGrantedAndUnknownTypeShouldNotExecuteAnyTask()
        throws WorkflowException {
        Map<String, Object> caseData = buildCaseData("I don't exist", YES_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        executeWorkflow(caseDetails);

        runNoTasksAtAll();
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

    private void runNoTasksToSendEmails() {
        runNoTasksToSendApprovedEmails();
        runNoTasksToSendNotApprovedEmails();
    }

    private void runNoTasksToSendNotApprovedEmails() {
        verifyTasksWereNeverCalled(deemedNotApprovedEmailTask, dispensedNotApprovedEmailTask);
    }

    private void runNoTasksToSendApprovedEmails() {
        verifyTasksWereNeverCalled(
            deemedApprovedEmailTask,
            dispensedApprovedEmailTask
        );
    }

    private void runNoTasksAtAll() {
        runNoTasksToSendEmails();
    }

    private Map<String, Object> executeWorkflow(CaseDetails caseDetails)
        throws WorkflowException {
        Map<String, Object> returnedData = classUnderTest.run(caseDetails, AUTH_TOKEN);
        assertThat(returnedData, is(notNullValue()));

        return returnedData;
    }
}