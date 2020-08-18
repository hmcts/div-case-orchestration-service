package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceRefusalDecision;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedApprovedEmailNotificationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalDraftRemovalTask;

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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceRefusalDecision.DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceRefusalDecision.FINAL;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class ServiceDecisionMadeWorkflowTest {

    @InjectMocks
    private ServiceDecisionMadeWorkflow classUnderTest;

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

    @Mock
    DeemedApprovedEmailNotificationTask deemedApprovedEmailNotificationTask;

    @Test
    public void whenDeemedAndApplicationIsNotGrantedAndFinal() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DEEMED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        mockTasksExecution(caseData, deemedServiceRefusalOrderTask, serviceRefusalDraftRemovalTask);

        Map<String, Object> returnedData = executeWorkflow(caseDetails, FINAL);

        verifyTasksCalledInOrder(returnedData, deemedServiceRefusalOrderTask, serviceRefusalDraftRemovalTask);

        runNoTasksToGenerateDraftPdfs();
        runNoTasksToSendEmails();
        verifyTasksWereNeverCalled(dispensedServiceRefusalOrderTask);
    }

    @Test
    public void whenDispensedAndApplicationIsNotGrantedAndFinal() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DISPENSED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        mockTasksExecution(caseData, dispensedServiceRefusalOrderTask, serviceRefusalDraftRemovalTask);

        Map<String, Object> returnedData = executeWorkflow(caseDetails, FINAL);

        verifyTasksCalledInOrder(returnedData, dispensedServiceRefusalOrderTask, serviceRefusalDraftRemovalTask);

        runNoTasksToGenerateDraftPdfs();
        runNoTasksToSendEmails();
        verifyTasksWereNeverCalled(deemedServiceRefusalOrderTask);
    }

    @Test
    public void whenDeemedAndApplicationIsNotGrantedAndDraft() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DEEMED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        mockTasksExecution(caseData, deemedServiceRefusalOrderDraftTask);

        Map<String, Object> returnedData = executeWorkflow(caseDetails, DRAFT);

        verifyTaskWasCalled(returnedData, deemedServiceRefusalOrderDraftTask);

        runNoTasksToGenerateFinalPdfs();
        runNoTasksToSendEmails();
        verifyTasksWereNeverCalled(serviceRefusalDraftRemovalTask);
        verifyTasksWereNeverCalled(dispensedServiceRefusalOrderDraftTask);
    }

    @Test
    public void whenDispensedAndApplicationIsNotGrantedAndDraft() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DISPENSED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        mockTasksExecution(caseData, dispensedServiceRefusalOrderDraftTask);

        Map<String, Object> returnedData = executeWorkflow(caseDetails, DRAFT);

        verifyTaskWasCalled(returnedData, dispensedServiceRefusalOrderDraftTask);

        runNoTasksToGenerateFinalPdfs();
        runNoTasksToSendEmails();
        verifyTasksWereNeverCalled(serviceRefusalDraftRemovalTask);
        verifyTasksWereNeverCalled(deemedServiceRefusalOrderDraftTask);
    }

    @Test
    public void whenMakeServiceDecisionAndNotAwaitingServiceConsiderationNoTasksShouldRun() throws WorkflowException {
        Map<String, Object> caseData = ImmutableMap.of("anyKey", "anyValue");
        CaseDetails caseDetails = buildCaseDetails(caseData, "stateOtherThanExpected");

        executeWorkflow(caseDetails, FINAL);

        runNoTasksAtAll();
    }

    @Test
    public void whenMakeServiceDecisionAndNotAwaitingServiceConsiderationAndDraftNoTasksShouldRun() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData("other", NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        executeWorkflow(caseDetails, DRAFT);

        runNoTasksAtAll();
    }

    @Test
    public void whenServiceDecisionMadeAndServiceApplicationIsGrantedAndDeemedShouldSendEmail() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DEEMED, YES_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        mockTasksExecution(caseData, deemedApprovedEmailNotificationTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails, FINAL);

        verifyTaskWasCalled(returnedCaseData, deemedApprovedEmailNotificationTask);

        runNoTasksToGeneratePdfs();
    }

    @Test
    public void whenServiceDecisionMadeAndServiceApplicationIsGrantedAndDispensedShouldNotSendEmailYet() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DISPENSED, YES_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        executeWorkflow(caseDetails, FINAL);

        // It will be changed when we implement more emails (this should happen in current sprint)
        runNoTasksAtAll();
    }

    @Test
    public void whenServiceDecisionMadeAndServiceApplicationIsNotGrantedAndAndTypeIsOtherDoNotGeneratePdfs() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData("someOtherValue", NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_SERVICE_CONSIDERATION);

        mockTasksExecution(caseData, serviceRefusalDraftRemovalTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails, FINAL);

        verifyTaskWasCalled(returnedCaseData, serviceRefusalDraftRemovalTask);

        runNoTasksToSendEmails();
        runNoTasksToGenerateDraftPdfs();
        runNoTasksToGenerateFinalPdfs();
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

    private void runNoTasksToGenerateFinalPdfs() {
        verifyTasksWereNeverCalled(
            deemedServiceRefusalOrderTask,
            dispensedServiceRefusalOrderTask
        );
    }

    private void runNoTasksToGeneratePdfs() {
        runNoTasksToGenerateDraftPdfs();
        runNoTasksToGenerateFinalPdfs();
        verifyTasksWereNeverCalled(serviceRefusalDraftRemovalTask);
    }

    private void runNoTasksToSendEmails() {
        verifyTasksWereNeverCalled(deemedApprovedEmailNotificationTask);
    }

    private void runNoTasksAtAll() {
        runNoTasksToGeneratePdfs();
        runNoTasksToSendEmails();
    }

    private Map<String, Object> executeWorkflow(CaseDetails caseDetails, ServiceRefusalDecision decision)
        throws WorkflowException {
        Map<String, Object> returnedData = classUnderTest.run(caseDetails, AUTH_TOKEN, decision);
        assertThat(returnedData, is(notNullValue()));

        return returnedData;
    }
}