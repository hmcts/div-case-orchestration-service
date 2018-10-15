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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_DEFENDS_DIVORCE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(MockitoJUnitRunner.class)
public class SendRespondentSubmissionNotificationWorkflowTest {

    private static final String UNFORMATTED_CASE_ID = "0123456789012345";

    private Map<String, Object> returnedPayloadFromTask;

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private SendRespondentSubmissionNotificationForDefendedDivorceEmail sendRespondentSubmissionNotificationForDefendedDivorceEmailTask;

    @Mock
    private SendRespondentSubmissionNotificationForUndefendedDivorceEmail sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @InjectMocks
    private SendRespondentSubmissionNotificationWorkflow workflow;

    @Before
    public void setUp() throws TaskException {
        returnedPayloadFromTask = new HashMap<>();
        when(sendRespondentSubmissionNotificationForDefendedDivorceEmailTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
        when(sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask.execute(any(), any())).thenReturn(returnedPayloadFromTask);
    }

    @Test
    public void testDefendedTaskIsCalledWhenWorkflowIsRun() throws WorkflowException, IOException, TaskException {
        CreateEvent caseRequestDetails = getJsonFromResourceFile("/jsonExamples/payloads/respondentAcknowledgesServiceDefendingDivorce.json", CreateEvent.class);
        Map<String, Object> caseData = caseRequestDetails.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = workflow.run(caseRequestDetails);

        verify(sendRespondentSubmissionNotificationForDefendedDivorceEmailTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
        verifyZeroInteractions(sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask);
        assertThat(returnedPayloadFromWorkflow, is(sameInstance(returnedPayloadFromTask)));
        TaskContext taskContextPassedToTask = taskContextArgumentCaptor.getValue();
        String caseIdPassedToTaskInWorkflowContext = (String) taskContextPassedToTask.getTransientObject(CASE_ID_JSON_KEY);
        assertThat(caseIdPassedToTaskInWorkflowContext, is(equalTo(UNFORMATTED_CASE_ID)));
    }

    @Test
    public void testUndefendedTaskIsCalled_WhenRespondentChoosesToNotDefendDivorce() throws IOException, WorkflowException, TaskException {
        CreateEvent caseRequestDetails = getJsonFromResourceFile("/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingDivorce.json", CreateEvent.class);
        Map<String, Object> caseData = caseRequestDetails.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = workflow.run(caseRequestDetails);

        verify(sendRespondentSubmissionNotificationForUndefendedDivorceEmailTask).execute(taskContextArgumentCaptor.capture(), same(caseData));
        verifyZeroInteractions(sendRespondentSubmissionNotificationForDefendedDivorceEmailTask);
        assertThat(returnedPayloadFromWorkflow, is(sameInstance(returnedPayloadFromTask)));
        TaskContext taskContextPassedToTask = taskContextArgumentCaptor.getValue();
        String caseIdPassedToTaskInWorkflowContext = (String) taskContextPassedToTask.getTransientObject(CASE_ID_JSON_KEY);
        assertThat(caseIdPassedToTaskInWorkflowContext, is(equalTo(UNFORMATTED_CASE_ID)));
    }

    @Test
    public void testExceptionIsThrown_IfNotPossibleToAssert_WhetherDivorceWillBeDefended() throws IOException, WorkflowException {
        expectedException.expect(WorkflowException.class);
        expectedException.expectMessage("Could not evaluate value of property \"" + RESP_DEFENDS_DIVORCE_CCD_FIELD + "\"");

        CreateEvent caseRequestDetails = getJsonFromResourceFile("/jsonExamples/payloads/faultyAcknowledgementOfService.json", CreateEvent.class);
        Map<String, Object> incomingCaseDate = caseRequestDetails.getCaseDetails().getCaseData();

        Map<String, Object> returnedPayloadFromWorkflow = workflow.run(caseRequestDetails);

        assertThat(returnedPayloadFromWorkflow.size(), is(incomingCaseDate.size() + 1));
    }

}