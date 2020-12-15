package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedPetitionerEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedSolicitorEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedNotApprovedPetitionerEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedNotApprovedSolicitorEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedApprovedPetitionerEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedApprovedSolicitorEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedNotApprovedPetitionerEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedNotApprovedSolicitorEmailTask;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATIONS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DEEMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DISPENSED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractorTest.buildCollectionMember;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksWereNeverCalled;

@RunWith(MockitoJUnitRunner.class)
public class ServiceDecisionMadeWorkflowTest {

    @InjectMocks
    private ServiceDecisionMadeWorkflow classUnderTest;

    @Mock
    private DeemedApprovedPetitionerEmailTask deemedApprovedPetitionerEmailTask;

    @Mock
    private DeemedApprovedSolicitorEmailTask deemedApprovedSolicitorEmailTask;

    @Mock
    private DeemedNotApprovedPetitionerEmailTask deemedNotApprovedPetitionerEmailTask;

    @Mock
    private DeemedNotApprovedSolicitorEmailTask deemedNotApprovedSolicitorEmailTask;

    @Mock
    private DispensedApprovedPetitionerEmailTask dispensedApprovedPetitionerEmailTask;

    @Mock
    private DispensedApprovedSolicitorEmailTask dispensedApprovedSolicitorEmailTask;

    @Mock
    private DispensedNotApprovedPetitionerEmailTask dispensedNotApprovedPetitionerEmailTask;

    @Mock
    private DispensedNotApprovedSolicitorEmailTask dispensedNotApprovedSolicitorEmailTask;

    @Test
    public void whenDeemedAndApplicationIsNotGranted() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DEEMED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, SERVICE_APPLICATION_NOT_APPROVED);

        mockTasksExecution(
            caseData,
            deemedNotApprovedPetitionerEmailTask
        );

        Map<String, Object> returnedData = executeWorkflow(caseDetails);

        verifyTasksCalledInOrder(
            returnedData,
            deemedNotApprovedPetitionerEmailTask
        );

        runNoTasksToSendApprovedEmails();
        verifyTasksWereNeverCalled(dispensedNotApprovedPetitionerEmailTask);
    }

    @Test
    public void whenDispensedAndApplicationIsNotGranted() throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DISPENSED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, SERVICE_APPLICATION_NOT_APPROVED);

        mockTasksExecution(
            caseData,
            dispensedNotApprovedPetitionerEmailTask
        );

        Map<String, Object> returnedData = executeWorkflow(caseDetails);

        verifyTasksCalledInOrder(
            returnedData,
            dispensedNotApprovedPetitionerEmailTask
        );

        runNoTasksToSendApprovedEmails();
        verifyTasksWereNeverCalled(deemedNotApprovedPetitionerEmailTask);
    }

    @Test
    public void shouldSendDeemedApprovedEmail_ToPetitioner_whenServiceApplicationIsGrantedAndDeemed()
        throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DEEMED, YES_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        mockTasksExecution(caseData, deemedApprovedPetitionerEmailTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, deemedApprovedPetitionerEmailTask);

        verifyTasksWereNeverCalled(deemedApprovedSolicitorEmailTask, dispensedApprovedSolicitorEmailTask);
        runNoTasksToSendNotApprovedEmails();
    }

    @Test
    public void shouldSendDeemedApprovedEmail_ToSolicitor_whenServiceApplicationIsGrantedAndDeemed()
        throws WorkflowException {
        Map<String, Object> caseData = petitionerRepresented(buildCaseData(DEEMED, YES_VALUE));
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        mockTasksExecution(caseData, deemedApprovedSolicitorEmailTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, deemedApprovedSolicitorEmailTask);

        verifyTasksWereNeverCalled(deemedApprovedPetitionerEmailTask, dispensedApprovedSolicitorEmailTask);
        runNoTasksToSendNotApprovedEmails();
    }

    @Test
    public void shouldSendDispensedApprovedEmail_ToPetitioner_whenApplicationIsGrantedAndDispensed()
        throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DISPENSED, YES_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        mockTasksExecution(caseData, dispensedApprovedPetitionerEmailTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, dispensedApprovedPetitionerEmailTask);

        verifyTasksWereNeverCalled(dispensedApprovedSolicitorEmailTask, deemedApprovedSolicitorEmailTask);
        runNoTasksToSendNotApprovedEmails();
    }

    @Test
    public void shouldSendDispensedApprovedEmail_ToSolicitor_whenApplicationIsGrantedAndDispensed()
        throws WorkflowException {
        Map<String, Object> caseData = petitionerRepresented(buildCaseData(DISPENSED, YES_VALUE));
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        mockTasksExecution(caseData, dispensedApprovedSolicitorEmailTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, dispensedApprovedSolicitorEmailTask);

        verifyTasksWereNeverCalled(dispensedApprovedPetitionerEmailTask, deemedApprovedSolicitorEmailTask);
        runNoTasksToSendNotApprovedEmails();
    }

    @Test
    public void shouldSendDeemedNotApprovedEmail_ToPetitioner_whenApplicationIsNotGrantedAndDeemed()
        throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DEEMED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        mockTasksExecution(caseData, deemedNotApprovedPetitionerEmailTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, deemedNotApprovedPetitionerEmailTask);

        verifyTasksWereNeverCalled(dispensedApprovedSolicitorEmailTask, deemedApprovedSolicitorEmailTask);
        runNoTasksToSendApprovedEmails();
    }

    @Test
    public void shouldSendDeemedNotApprovedEmail_ToSolicitor_whenApplicationIsNotGrantedAndDeemed()
        throws WorkflowException {
        Map<String, Object> caseData = petitionerRepresented(buildCaseData(DEEMED, NO_VALUE));
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        mockTasksExecution(caseData, deemedNotApprovedSolicitorEmailTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, deemedNotApprovedSolicitorEmailTask);

        verifyTasksWereNeverCalled(dispensedApprovedPetitionerEmailTask, deemedApprovedSolicitorEmailTask);
        runNoTasksToSendApprovedEmails();
    }

    @Test
    public void shouldSendDispensedNotApprovedEmail_ToPetitioner_whenApplicationIsNotGrantedAndDispensed()
        throws WorkflowException {
        Map<String, Object> caseData = buildCaseData(DISPENSED, NO_VALUE);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        mockTasksExecution(caseData, dispensedNotApprovedPetitionerEmailTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, dispensedNotApprovedPetitionerEmailTask);

        verifyTasksWereNeverCalled(dispensedNotApprovedSolicitorEmailTask, deemedApprovedSolicitorEmailTask);
        runNoTasksToSendApprovedEmails();
    }

    @Test
    public void shouldSendDispensedNotApprovedEmail_ToSolicitor_whenApplicationIsNotGrantedAndDispensed()
        throws WorkflowException {
        Map<String, Object> caseData = petitionerRepresented(buildCaseData(DISPENSED, NO_VALUE));
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        mockTasksExecution(caseData, dispensedNotApprovedSolicitorEmailTask);

        Map<String, Object> returnedCaseData = executeWorkflow(caseDetails);

        verifyTaskWasCalled(returnedCaseData, dispensedNotApprovedSolicitorEmailTask);

        verifyTasksWereNeverCalled(dispensedNotApprovedPetitionerEmailTask, deemedApprovedSolicitorEmailTask);
        runNoTasksToSendApprovedEmails();
    }

    @Test
    public void whenApplicationIsGrantedAndUnknownTypeShouldNotExecuteAnyTask()
        throws WorkflowException {
        runTestWithUnknownServiceApplicartionTypeToVerifyNoTaskIsExecutedForGranted(YES_VALUE);
    }

    @Test
    public void whenApplicationIsNotGrantedAndUnknownTypeShouldNotExecuteAnyTask()
        throws WorkflowException {
        runTestWithUnknownServiceApplicartionTypeToVerifyNoTaskIsExecutedForGranted(NO_VALUE);
    }

    private void runTestWithUnknownServiceApplicartionTypeToVerifyNoTaskIsExecutedForGranted(String granted)
        throws WorkflowException {
        Map<String, Object> caseData = buildCaseData("I don't exist", granted);
        CaseDetails caseDetails = buildCaseDetails(caseData, AWAITING_DECREE_NISI);

        executeWorkflow(caseDetails);

        runNoTasksAtAll();
    }

    public static Map<String, Object> petitionerRepresented(Map<String, Object> caseData) {
        Map<String, Object> updatedCaseData = new HashMap<>(caseData);
        updatedCaseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        updatedCaseData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);

        return updatedCaseData;
    }

    private Map<String, Object> buildCaseData(String serviceApplicationType, String serviceApplicationGranted) {
        return ImmutableMap.of(
            SERVICE_APPLICATIONS, asList(buildCollectionMember(serviceApplicationGranted, serviceApplicationType))
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
        verifyTasksWereNeverCalled(
            deemedNotApprovedPetitionerEmailTask,
            deemedNotApprovedSolicitorEmailTask,
            dispensedNotApprovedSolicitorEmailTask,
            dispensedNotApprovedPetitionerEmailTask
        );
    }

    private void runNoTasksToSendApprovedEmails() {
        verifyTasksWereNeverCalled(
            deemedApprovedPetitionerEmailTask,
            deemedApprovedSolicitorEmailTask,
            dispensedApprovedPetitionerEmailTask,
            dispensedApprovedSolicitorEmailTask
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
