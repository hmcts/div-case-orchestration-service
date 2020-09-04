package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseWithIdTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.LinkRespondent;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrievePinUserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetSolicitorLinkedField;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateExistingSolicitorLink;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;

@RunWith(MockitoJUnitRunner.class)
public class RespondentSolicitorLinkCaseWorkflowTest {

    private static final String CASE_REFERENCE = "CaseReference";
    private static final String RESPONDENT_SOLICITOR_CASE_NO = "RespondentSolicitorCaseNo";
    private static final String RESPONDENT_SOLICITOR_PIN = "RespondentSolicitorPin";

    @Mock
    private GetCaseWithIdTask getCaseWithId;

    @Mock
    private ValidateExistingSolicitorLink validateExistingSolicitorLink;

    @Mock
    private RetrievePinUserDetails retrievePinUserDetails;

    @Mock
    private LinkRespondent linkRespondent;

    @Mock
    private SetSolicitorLinkedField setSolicitorLinkedField;

    @InjectMocks
    private RespondentSolicitorLinkCaseWorkflow respondentSolicitorLinkCaseWorkflow;

    private CaseDetails caseDetails;
    private TaskContext context;

    @Before
    public void setUp() {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> respondentSolicitorCaseLink = new HashMap<>();
        respondentSolicitorCaseLink.put(CASE_REFERENCE, TEST_CASE_ID);
        payload.put(RESPONDENT_SOLICITOR_CASE_NO, respondentSolicitorCaseLink);
        payload.put(RESPONDENT_SOLICITOR_PIN, TEST_PIN);
        caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .caseData(payload)
                .build();
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, TEST_TOKEN);
        context.setTransientObject(RESPONDENT_PIN, TEST_PIN);
    }

    @Test
    public void runCallsTheFiveCorrectTasksInTheRightOrder() throws WorkflowException, TaskException {
        final UserDetails userDetails = UserDetails.builder().build();

        when(getCaseWithId.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(validateExistingSolicitorLink.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(retrievePinUserDetails.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(linkRespondent.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(setSolicitorLinkedField.execute(any(), eq(userDetails))).thenReturn(userDetails);

        UserDetails actual = respondentSolicitorLinkCaseWorkflow.run(caseDetails, TEST_TOKEN);

        assertThat(actual, is(userDetails));
        InOrder inOrder = inOrder(getCaseWithId, validateExistingSolicitorLink, retrievePinUserDetails, linkRespondent, setSolicitorLinkedField);
        inOrder.verify(getCaseWithId).execute(context, userDetails);
        inOrder.verify(validateExistingSolicitorLink).execute(context, userDetails);
        inOrder.verify(retrievePinUserDetails).execute(context, userDetails);
        inOrder.verify(linkRespondent).execute(context, userDetails);
        inOrder.verify(setSolicitorLinkedField).execute(context, userDetails);
    }

    @Test(expected = WorkflowException.class)
    public void caseNotFoundIsWrappedInWorkflowException() throws WorkflowException, TaskException {
        final UserDetails userDetails = UserDetails.builder().build();

        when(getCaseWithId.execute(any(), eq(userDetails))).thenThrow(new FeignException.NotFound("test", null));

        respondentSolicitorLinkCaseWorkflow.run(caseDetails, TEST_TOKEN);
    }

    @Test(expected = WorkflowException.class)
    public void authorisationErrorIsWrappedInWorkflowException() throws WorkflowException, TaskException {
        final UserDetails userDetails = UserDetails.builder().build();

        when(getCaseWithId.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(validateExistingSolicitorLink.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(retrievePinUserDetails.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(linkRespondent.execute(any(), eq(userDetails))).thenThrow(new FeignException.Unauthorized("test", null));

        respondentSolicitorLinkCaseWorkflow.run(caseDetails, TEST_TOKEN);
    }

    @Test(expected = FeignException.class)
    public void otherExceptionsNotWrappedInWorkflowException() throws WorkflowException, TaskException {
        final UserDetails userDetails = UserDetails.builder().build();

        when(getCaseWithId.execute(any(), eq(userDetails))).thenThrow(new FeignException.GatewayTimeout("test", null));

        respondentSolicitorLinkCaseWorkflow.run(caseDetails, TEST_TOKEN);
    }
}
