package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalDraftRemovalTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedNotApprovedEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedApprovedEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedNotApprovedEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.SolicitorDeemedApprovedEmailTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
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
    private DeemedServiceRefusalOrderTask deemedServiceRefusalOrderTask;

    @Mock
    private DispensedServiceRefusalOrderTask dispensedServiceRefusalOrderTask;

    @Mock
    private ServiceRefusalDraftRemovalTask serviceRefusalDraftRemovalTask;

    @Mock
    private SolicitorDeemedApprovedEmailTask solicitorDeemedApprovedEmailTask;

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
            deemedServiceRefusalOrderTask,
            deemedNotApprovedEmailTask,
            serviceRefusalDraftRemovalTask
        );

        Map<String, Object> returnedData = executeWorkflow(caseDetails);

        verifyTasksCalledInOrder(
            returnedData,
            deemedServiceRefusalOrderTask,
            deemedNotApprovedEmailTask,
            serviceRefusalDraftRemovalTask
        );

        runNoTasksToSendApprovedEmails();
        verifyTasksWereNeverCalled(dispensedNotApprovedEmailTask);
        verifyTasksWereNeverCalled(dispensedServiceRefusalOrderTask);
    }

    @Test
    public void whenDispensedAndApplicationIsNotGranted() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DISPENSED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, SERVICE_APPLICATION_NOT_APPROVED);

        mockTasksExecution(
            caseData,
            dispensedServiceRefusalOrderTask,
            dispensedNotApprovedEmailTask,
            serviceRefusalDraftRemovalTask
        );

        Map<String, Object> returnedData = executeWorkflow(caseDetails);

        verifyTasksCalledInOrder(
            returnedData,
            dispensedServiceRefusalOrderTask,
            dispensedNotApprovedEmailTask,
            serviceRefusalDraftRemovalTask
        );

        runNoTasksToSendApprovedEmails();
        verifyTasksWereNeverCalled(deemedNotApprovedEmailTask);
        verifyTasksWereNeverCalled(deemedServiceRefusalOrderTask);
    }

    @Test
    public void whenServiceApplicationIsGrantedAndDeemedShouldSendEmailToPetitioner() throws WorkflowException {
        Map<String, Object> caseData = petitionerRepresented(buildCaseData(DEEMED, YES_VALUE));
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        mockTasksExecution(caseData, solicitorDeemedApprovedEmailTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, solicitorDeemedApprovedEmailTask);

        verifyTasksWereNeverCalled(dispensedApprovedEmailTask, deemedApprovedEmailTask);
        runNoTasksToSendNotApprovedEmails();
        runNoTasksToGeneratePdfs();
    }

    @Test
    public void whenServiceApplicationIsGrantedAndDeemedAndRepresentedShouldSendEmailToSolicitor()
        throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DEEMED, YES_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        mockTasksExecution(caseData, deemedApprovedEmailTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, deemedApprovedEmailTask);

        verifyTaskWasNeverCalled(dispensedApprovedEmailTask);
        runNoTasksToSendNotApprovedEmails();
        runNoTasksToGeneratePdfs();
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
        runNoTasksToGeneratePdfs();
    }

    @Test
    public void whenApplicationIsGrantedAndUnknownTypeShouldNotExecuteAnyTask()
        throws WorkflowException {
        Map<String, Object> caseData = buildCaseData("I don't exist", YES_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        executeWorkflow(caseDetails);

        runNoTasksAtAll();
    }

    @Test
    public void whenServiceDecisionMadeAndServiceApplicationIsNotGrantedAndAndTypeIsOtherDoNotGeneratePdfs() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData("someOtherValue", NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, SERVICE_APPLICATION_NOT_APPROVED);

        mockTasksExecution(caseData, serviceRefusalDraftRemovalTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, serviceRefusalDraftRemovalTask);

        runNoTasksToSendEmails();
        runNoTasksToGenerateFinalPdfs();
    }

    public static Map<String, Object> petitionerRepresented(Map<String, Object> caseData) {
        Map<String, Object> updatedCaseData = new HashMap<>(caseData);
        updatedCaseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        updatedCaseData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);

        return updatedCaseData;
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

    private void runNoTasksToGenerateFinalPdfs() {
        verifyTasksWereNeverCalled(
            deemedServiceRefusalOrderTask,
            dispensedServiceRefusalOrderTask
        );
    }

    private void runNoTasksToGeneratePdfs() {
        runNoTasksToGenerateFinalPdfs();
        verifyTasksWereNeverCalled(serviceRefusalDraftRemovalTask);
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
            solicitorDeemedApprovedEmailTask,
            deemedApprovedEmailTask,
            dispensedApprovedEmailTask
        );
    }

    private void runNoTasksAtAll() {
        runNoTasksToGeneratePdfs();
        runNoTasksToSendEmails();
    }

    private Map<String, Object> executeWorkflow(CaseDetails caseDetails)
        throws WorkflowException {
        Map<String, Object> returnedData = classUnderTest.run(caseDetails, AUTH_TOKEN);
        assertThat(returnedData, is(notNullValue()));

        return returnedData;
    }
}
