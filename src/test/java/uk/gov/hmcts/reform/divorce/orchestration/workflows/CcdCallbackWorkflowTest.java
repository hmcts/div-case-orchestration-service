package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddPDF;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.IdamPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PetitionGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetIssueDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;

@RunWith(MockitoJUnitRunner.class)
public class CcdCallbackWorkflowTest {
    private CcdCallbackWorkflow ccdCallbackWorkflow;

    @Mock
    private SetIssueDate setIssueDate;

    @Mock
    private ValidateCaseData validateCaseData;

    @Mock
    private PetitionGenerator petitionGenerator;

    @Mock
    private RespondentLetterGenerator respondentLetterGenerator;

    @Mock
    private IdamPinGenerator idamPinGenerator;

    @Mock
    private CaseFormatterAddPDF caseFormatterAddPDF;

    private CreateEvent createEventRequest;
    private Map<String, Object> payload;
    private TaskContext context;

    @Before
    public void setUp() {
        ccdCallbackWorkflow =
                new CcdCallbackWorkflow(
                        validateCaseData,
                        setIssueDate,
                        petitionGenerator,
                        idamPinGenerator,
                        respondentLetterGenerator,
                    caseFormatterAddPDF);

        payload = new HashMap<>();
        payload.put("D8ScreenHasMarriageBroken", "YES");
        payload.put(PIN,TEST_PIN);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();
        createEventRequest =
                CreateEvent.builder()
                        .eventId(TEST_EVENT_ID)
                        .token(TEST_TOKEN)
                        .caseDetails(
                            caseDetails
                        )
                        .build();

        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
    }


    @Test
    public void givenAosInvitationGenerateIsTrue_whenRun_thenProceedAsExpected() throws WorkflowException {
        //Given
        when(setIssueDate.execute(context, payload)).thenReturn(payload);
        when(validateCaseData.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(idamPinGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentLetterGenerator.execute(context, payload)).thenReturn(payload);
        when(caseFormatterAddPDF.execute(context, payload)).thenReturn(payload);

        //When
        Map<String, Object> response = ccdCallbackWorkflow.run(createEventRequest, AUTH_TOKEN, true);

        //Then
        assertNotNull(response);
        assertEquals(2, response.size());
        assertTrue(response.containsKey(PIN));
    }

    @Test
    public void givenAosInvitationGenerateIsFalse_whenRun_thenProceedAsExpected() throws WorkflowException {
        //Given
        when(validateCaseData.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(idamPinGenerator.execute(context, payload)).thenReturn(payload);
        when(caseFormatterAddPDF.execute(context, payload)).thenReturn(payload);

        //When
        Map<String, Object> response = ccdCallbackWorkflow.run(createEventRequest, AUTH_TOKEN, false);

        //Then
        assertNotNull(response);
        assertEquals(2, response.size());
        assertTrue(response.containsKey(PIN));

        verifyZeroInteractions(respondentLetterGenerator);
    }

    @After
    public void tearDown() {
        ccdCallbackWorkflow = null;
    }
}