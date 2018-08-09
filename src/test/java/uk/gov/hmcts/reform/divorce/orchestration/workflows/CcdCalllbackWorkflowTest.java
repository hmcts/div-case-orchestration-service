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
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.IdamPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PetitionGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
public class CcdCalllbackWorkflowTest {
    private CcdCalllbackWorkflow ccdCalllbackWorkflow;

    @Mock
    private ValidateCaseData validateCaseData;

    @Mock
    private PetitionGenerator petitionGenerator;

    @Mock
    private RespondentLetterGenerator respondentLetterGenerator;

    @Mock
    private IdamPinGenerator idamPinGenerator;

    @Mock
    private CaseDataFormatter caseDataFormatter;

    private CreateEvent createEventRequest;
    private Map<String, Object> payload;
    private TaskContext context;
    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        ccdCalllbackWorkflow =
                new CcdCalllbackWorkflow(
                        validateCaseData,
                        petitionGenerator,
                        idamPinGenerator,
                        respondentLetterGenerator,
                        caseDataFormatter);

        payload = new HashMap<>();
        payload.put("D8ScreenHasMarriageBroken", "YES");
        payload.put(PIN,TEST_PIN );

        caseDetails = CaseDetails.builder()
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
    public void runShouldReturnValidCaseDataForValidCase() throws WorkflowException, TaskException {
        //Given
        when(validateCaseData.execute(context, payload)).thenReturn(payload);
        when(petitionGenerator.execute(context, payload)).thenReturn(payload);
        when(idamPinGenerator.execute(context, payload)).thenReturn(payload);
        when(respondentLetterGenerator.execute(context, payload)).thenReturn(payload);
        when(caseDataFormatter.execute(context, payload)).thenReturn(payload);

        //When
        Map<String, Object> response = ccdCalllbackWorkflow.run(createEventRequest, AUTH_TOKEN);

        //Then
        assertNotNull(response);
        assertEquals(2, response.size());
        assertTrue(response.containsKey(PIN));
    }

    @After
    public void tearDown() throws Exception {
        ccdCalllbackWorkflow = null;
    }
}