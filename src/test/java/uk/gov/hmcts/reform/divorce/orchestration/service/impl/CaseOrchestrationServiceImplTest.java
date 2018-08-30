package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCalllbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DeleteDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SaveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;


@RunWith(MockitoJUnitRunner.class)
public class CaseOrchestrationServiceImplTest {

    @Mock
    private SubmitToCCDWorkflow submitToCCDWorkflow;

    @Mock
    private CcdCalllbackWorkflow ccdCallbackWorkflow;

    @Mock
    private RetrieveDraftWorkflow retrieveDraftWorkflow;

    @Mock
    private SaveDraftWorkflow saveDraftWorkflow;

    @Mock
    private DeleteDraftWorkflow deleteDraftWorkflow;

    @InjectMocks
    private CaseOrchestrationServiceImpl service;

    private CreateEvent createEventRequest;

    private Map<String, Object> expectedPayload;


    @Before
    public void setUp() {
        createEventRequest = CreateEvent.builder()
                .caseDetails(
                        CaseDetails.builder()
                                .caseData(new HashMap<>())
                                .caseId(TEST_CASE_ID)
                                .state(TEST_STATE)
                                .build())
                .eventId(TEST_EVENT_ID)
                .token(TEST_TOKEN)
                .build();
        expectedPayload = new HashMap<>();
        expectedPayload.put(PIN, TEST_PIN);
    }

    @Test
    public void ccdCallbackHandlerShouldReturnValidCaseDataForValidRequest()
            throws WorkflowException {
        //given
        when(ccdCallbackWorkflow.run(createEventRequest, AUTH_TOKEN)).thenReturn(expectedPayload);

        //when
        Map<String, Object> actual = service.ccdCallbackHandler(createEventRequest, AUTH_TOKEN);

        //then
        assertEquals(expectedPayload, actual);
        assertEquals(expectedPayload.get(PIN), TEST_PIN);
    }

    @Test
    public void givenDraftInWorkflowResponse_whenGetDraft_thenReturnPayloadFromWorkflow() throws WorkflowException {
        Map<String, Object> testExpectedPayload = mock(Map.class);

        when(retrieveDraftWorkflow.run(AUTH_TOKEN)).thenReturn(testExpectedPayload);
        assertEquals(testExpectedPayload,service.getDraft(AUTH_TOKEN));
    }

    @Test
    public void givenErrorOnDraftWorkflow_whenGetDraft_thenReturnErrors() throws WorkflowException {
        Map<String, Object> expectedErrors = mock(Map.class);
        Map<String, Object> workflowResponsePayload = mock(Map.class);

        when(retrieveDraftWorkflow.run(AUTH_TOKEN)).thenReturn(workflowResponsePayload);
        when(retrieveDraftWorkflow.errors()).thenReturn(expectedErrors);

        assertEquals(expectedErrors, service.getDraft(AUTH_TOKEN));
    }

    @Test
    public void whenSaveDraft_thenReturnPayloadFromWorkflow() throws WorkflowException {
        Map<String, Object> payload = mock(Map.class);
        Map<String, Object> testExpectedPayload = mock(Map.class);

        when(saveDraftWorkflow.run(payload,AUTH_TOKEN, TEST_USER_EMAIL)).thenReturn(testExpectedPayload);
        assertEquals(testExpectedPayload,service.saveDraft(payload, AUTH_TOKEN, TEST_USER_EMAIL));
    }

    @Test
    public void givenErrorOnDraftWorkflow_whenSaveDraft_thenReturnErrors() throws WorkflowException {
        Map<String, Object> expectedErrors = mock(Map.class);
        Map<String, Object> payload = mock(Map.class);
        Map<String, Object> workflowResponsePayload = mock(Map.class);


        when(saveDraftWorkflow.run(payload,AUTH_TOKEN, TEST_USER_EMAIL)).thenReturn(workflowResponsePayload);
        when(saveDraftWorkflow.errors()).thenReturn(expectedErrors);

        assertEquals(expectedErrors,service.saveDraft(payload, AUTH_TOKEN, TEST_USER_EMAIL));
    }

    @Test
    public void givenUserWithADraft_whenDeleteDraft_thenReturnPayloadFromWorkflow() throws WorkflowException {
        Map<String, Object> testExpectedPayload = mock(Map.class);
        when(deleteDraftWorkflow.run(AUTH_TOKEN)).thenReturn(testExpectedPayload);
        assertEquals(testExpectedPayload,service.deleteDraft(AUTH_TOKEN));
    }

    @Test
    public void givenErrorOnDraftWorkflow_whenDeleteDraft_thenReturnErrors() throws WorkflowException {
        Map<String, Object> expectedErrors = mock(Map.class);
        Map<String, Object> workflowResponsePayload = mock(Map.class);

        when(deleteDraftWorkflow.run(AUTH_TOKEN)).thenReturn(workflowResponsePayload);
        when(deleteDraftWorkflow.errors()).thenReturn(expectedErrors);

        assertEquals(expectedErrors, service.deleteDraft(AUTH_TOKEN));
    }

    @After
    public void tearDown() {
        createEventRequest = null;
        expectedPayload = null;
    }

}