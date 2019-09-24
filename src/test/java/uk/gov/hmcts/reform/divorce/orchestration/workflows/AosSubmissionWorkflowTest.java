package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.QueueAosSolicitorSubmitTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForDefendedDivorceEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSubmissionNotificationForUndefendedDivorceEmail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(MockitoJUnitRunner.class)
public class AosSubmissionWorkflowTest {

    private static final String UNFORMATTED_CASE_ID = "0123456789012345";
    private static final String RESP_ACKNOWLEDGES_SERVICE_DEFENDING_DIVORCE_JSON =
            "/jsonExamples/payloads/respondentAcknowledgesServiceDefendingDivorce.json";
    private static final String RESP_ACKNOWLEDGES_SERVICE__NOT_DEFENDING_DIVORCE_JSON =
            "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingDivorce.json";
    private static final String RESP_ACKNOWLEDGES_SERVICE__NOT_DEFENDING__NOT_ADMITTING_DIVORCE_JSON =
            "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingNotAdmittingDivorce.json";
    private static final String UNCLEAR_ACKNOWLEDGEMENT_OF_SERVICE_JSON =
            "/jsonExamples/payloads/unclearAcknowledgementOfService.json";
    private static final String AOS_SOLICITOR_NOMINATED_JSON =
            "/jsonExamples/payloads/aosSolicitorNominated.json";
    private static final String AOS_SOLICITOR_NOMINATED_WITHOUT_FIELDS_SET_JSON =
            "/jsonExamples/payloads/aosSolicitorNominatedWithoutFieldSet.json";

    private Map<String, Object> returnedPayloadFromTask;

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private SendRespondentSubmissionNotificationForDefendedDivorceEmail defendedDivorceNotificationEmailTask;

    @Mock
    private SendRespondentSubmissionNotificationForUndefendedDivorceEmail undefendedDivorceNotificationEmailTask;

    @Mock
    private QueueAosSolicitorSubmitTask queueAosSolicitorSubmitTask;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @InjectMocks
    private AosSubmissionWorkflow aosSubmissionWorkflow;

    @Before
    public void setUp() throws TaskException {
        returnedPayloadFromTask = new HashMap<>();
        when(defendedDivorceNotificationEmailTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
        when(undefendedDivorceNotificationEmailTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
        when(queueAosSolicitorSubmitTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
    }

    @Test
    public void testDefendedTaskIsCalledWhenWorkflowIsRun() throws WorkflowException, IOException, TaskException {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                RESP_ACKNOWLEDGES_SERVICE_DEFENDING_DIVORCE_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(defendedDivorceNotificationEmailTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
        verifyZeroInteractions(undefendedDivorceNotificationEmailTask);
        assertThat(returnedPayloadFromWorkflow, is(sameInstance(returnedPayloadFromTask)));
        TaskContext taskContextPassedToTask = taskContextArgumentCaptor.getValue();
        String caseIdPassedToTask = taskContextPassedToTask.getTransientObject(CASE_ID_JSON_KEY);
        assertThat(caseIdPassedToTask, is(equalTo(UNFORMATTED_CASE_ID)));
    }

    @Test
    public void testUndefendedTaskIsCalled_WhenRespondentChoosesToNotDefendDivorce() throws IOException,
            WorkflowException, TaskException {
        CcdCallbackRequest callbackRequest = getJsonFromResourceFile(
                RESP_ACKNOWLEDGES_SERVICE__NOT_DEFENDING_DIVORCE_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = aosSubmissionWorkflow.run(callbackRequest, AUTH_TOKEN);

        verify(undefendedDivorceNotificationEmailTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
        verifyZeroInteractions(defendedDivorceNotificationEmailTask);
        assertThat(returnedPayloadFromWorkflow, is(sameInstance(returnedPayloadFromTask)));
        TaskContext taskContextPassedToTask = taskContextArgumentCaptor.getValue();
        String caseIdPassedToTask = taskContextPassedToTask.getTransientObject(CASE_ID_JSON_KEY);
        assertThat(caseIdPassedToTask, is(equalTo(UNFORMATTED_CASE_ID)));
    }

    @Test
    public void testUndefendedTaskIsCalled_WhenRespondentChoosesToNotDefendDivorceButNotAdmitWhatIsSaid()
            throws IOException, WorkflowException, TaskException {
        CcdCallbackRequest callbackRequest = getJsonFromResourceFile(
                RESP_ACKNOWLEDGES_SERVICE__NOT_DEFENDING__NOT_ADMITTING_DIVORCE_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = aosSubmissionWorkflow.run(callbackRequest, AUTH_TOKEN);

        verify(undefendedDivorceNotificationEmailTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
        verifyZeroInteractions(defendedDivorceNotificationEmailTask);
        assertThat(returnedPayloadFromWorkflow, is(sameInstance(returnedPayloadFromTask)));
        TaskContext taskContextPassedToTask = taskContextArgumentCaptor.getValue();
        String caseIdPassedToTask = taskContextPassedToTask.getTransientObject(CASE_ID_JSON_KEY);
        assertThat(caseIdPassedToTask, is(equalTo(UNFORMATTED_CASE_ID)));
    }

    @Test
    public void testExceptionIsThrown_IfNotPossibleToAssert_WhetherDivorceWillBeDefended() throws IOException,
            WorkflowException {
        expectedException.expect(WorkflowException.class);
        expectedException.expectMessage(String.format("%s field doesn't contain a valid value",
            RESP_WILL_DEFEND_DIVORCE));

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                UNCLEAR_ACKNOWLEDGEMENT_OF_SERVICE_JSON, CcdCallbackRequest.class);
        Map<String, Object> incomingCaseDate = ccdCallbackRequest.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        assertThat(returnedPayloadFromWorkflow.size(), is(incomingCaseDate.size() + 1));
    }

    @Test
    public void testSolicitorTaskIsCalledWhenWorkflowIsRun_whenSolicitorIsRepresenting()
            throws WorkflowException, IOException, TaskException {

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                AOS_SOLICITOR_NOMINATED_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(queueAosSolicitorSubmitTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
    }

    @Test
    public void testSolicitorTaskIsNotCalledWhenSolicitorIsNotRepresenting() throws IOException,
            WorkflowException {

        CcdCallbackRequest callbackRequest = getJsonFromResourceFile(
                RESP_ACKNOWLEDGES_SERVICE__NOT_DEFENDING_DIVORCE_JSON, CcdCallbackRequest.class);

        aosSubmissionWorkflow.run(callbackRequest, AUTH_TOKEN);

        verifyZeroInteractions(queueAosSolicitorSubmitTask);
    }

    @Test
    public void testSolicitorTaskIsCalled_whenSolicitorIsRepresentingIsEmpty_andRespSolValuesExist()
            throws WorkflowException, IOException, TaskException {

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                AOS_SOLICITOR_NOMINATED_WITHOUT_FIELDS_SET_JSON, CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        aosSubmissionWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        verify(queueAosSolicitorSubmitTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
    }
}
