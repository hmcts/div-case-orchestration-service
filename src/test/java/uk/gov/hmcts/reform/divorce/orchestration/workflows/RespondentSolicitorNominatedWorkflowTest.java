package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ResetRespondentLinkingFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSolicitorAosInvitationEmail;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class RespondentSolicitorNominatedWorkflowTest {

    private RespondentSolicitorNominatedWorkflow respondentSolicitorNominatedWorkflow;

    @Mock
    private RespondentPinGenerator respondentPinGenerator;

    @Mock
    private ResetRespondentLinkingFields resetRespondentLinkingFields;

    @Mock
    private SendRespondentSolicitorAosInvitationEmail sendRespondentSolicitorNotificationEmail;

    private CaseDetails caseDetails;
    private Map<String, Object> payload;
    private TaskContext context;

    @Before
    public void setUp() {
        respondentSolicitorNominatedWorkflow = new RespondentSolicitorNominatedWorkflow(
                respondentPinGenerator,
                sendRespondentSolicitorNotificationEmail,
                resetRespondentLinkingFields
        );

        payload = new HashMap<>();

        caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .caseData(payload)
                .build();

        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void testRunCallsTheRequiredTasks() throws WorkflowException, TaskException {
        //Given
        when(respondentPinGenerator.execute(context, payload)).thenReturn(payload);
        when(sendRespondentSolicitorNotificationEmail.execute(context, payload)).thenReturn(payload);
        when(resetRespondentLinkingFields.execute(context, payload)).thenReturn(payload);

        //When
        Map<String, Object> response = respondentSolicitorNominatedWorkflow.run(caseDetails);

        //Then
        InOrder inOrder = inOrder(respondentPinGenerator, sendRespondentSolicitorNotificationEmail, resetRespondentLinkingFields);
        assertThat(response, is(payload));
        inOrder.verify(respondentPinGenerator).execute(context, payload);
        inOrder.verify(sendRespondentSolicitorNotificationEmail).execute(context, payload);
        inOrder.verify(resetRespondentLinkingFields).execute(context, payload);
    }
}