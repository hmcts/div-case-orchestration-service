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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(MockitoJUnitRunner.class)
public class SendRespondentSubmissionNotificationWorkflowTest {

    private static final String UNFORMATTED_CASE_ID = "0123456789012345";

    private Map<String, Object> returnedPayloadFromTask;

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private SendRespondentSubmissionNotificationForDefendedDivorceEmail defendedDivorceNotificationEmailTask;

    @Mock
    private SendRespondentSubmissionNotificationForUndefendedDivorceEmail undefendedDivorceNotificationEmailTask;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @InjectMocks
    private SendRespondentSubmissionNotificationWorkflow workflow;

    @Before
    public void setUp() throws TaskException {
        returnedPayloadFromTask = new HashMap<>();
        when(defendedDivorceNotificationEmailTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
        when(undefendedDivorceNotificationEmailTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
    }

    @Test
    public void testDefendedTaskIsCalledWhenWorkflowIsRun() throws WorkflowException, IOException, TaskException {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/respondentAcknowledgesServiceDefendingDivorce.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = workflow.run(ccdCallbackRequest);

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
                "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingDivorce.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = workflow.run(callbackRequest);

        verify(undefendedDivorceNotificationEmailTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
        verifyZeroInteractions(defendedDivorceNotificationEmailTask);
        assertThat(returnedPayloadFromWorkflow, is(sameInstance(returnedPayloadFromTask)));
        TaskContext taskContextPassedToTask = taskContextArgumentCaptor.getValue();
        String caseIdPassedToTask = taskContextPassedToTask.getTransientObject(CASE_ID_JSON_KEY);
        assertThat(caseIdPassedToTask, is(equalTo(UNFORMATTED_CASE_ID)));
    }

    @Test
    public void testUndefendedTaskIsCalled_WhenRespondentChoosesToNotDefendDivorceButNotAdmitWhatIsSaid() throws IOException,
            WorkflowException, TaskException {
        CcdCallbackRequest callbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingNotAdmittingDivorce.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = workflow.run(callbackRequest);

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
                "/jsonExamples/payloads/unclearAcknowledgementOfService.json", CcdCallbackRequest.class);
        Map<String, Object> incomingCaseDate = ccdCallbackRequest.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = workflow.run(ccdCallbackRequest);

        assertThat(returnedPayloadFromWorkflow.size(), is(incomingCaseDate.size() + 1));
    }
}
