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
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AuthenticateRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCalllbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateToCCDWorkflow;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;


@RunWith(MockitoJUnitRunner.class)
public class CaseOrchestrationServiceImplTest {

    @Mock
    private CcdCalllbackWorkflow ccdCallbackWorkflow;

    @Mock
    private AuthenticateRespondentWorkflow authenticateRespondentWorkflow;

    @Mock
    private SubmitToCCDWorkflow submitToCCDWorkflow;

    @Mock
    private UpdateToCCDWorkflow updateToCCDWorkflow;

    @InjectMocks
    private CaseOrchestrationServiceImpl classUnderTest;

    private CreateEvent createEventRequest;

    private Map<String, Object> requestPayload;

    private Map<String, Object> expectedPayload;

    @Before
    public void setUp() {
        createEventRequest = CreateEvent.builder()
                .caseDetails(
                        CaseDetails.builder()
                                .caseData(Collections.emptyMap())
                                .caseId(TEST_CASE_ID)
                                .state(TEST_STATE)
                                .build())
                .eventId(TEST_EVENT_ID)
                .token(TEST_TOKEN)
                .build();
        requestPayload = Collections.emptyMap();
        expectedPayload = Collections.singletonMap(PIN, TEST_PIN);
    }

    @Test
    public void ccdCallbackHandlerShouldReturnValidCaseDataForValidRequest()
            throws WorkflowException {
        //given
        when(ccdCallbackWorkflow.run(createEventRequest, AUTH_TOKEN)).thenReturn(expectedPayload);

        //when
        Map<String, Object> actual = classUnderTest.ccdCallbackHandler(createEventRequest, AUTH_TOKEN);

        //then
        assertEquals(expectedPayload, actual);
        assertEquals(expectedPayload.get(PIN), TEST_PIN);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void whenAuthenticateRespondent_thenProceedAsExpected() throws WorkflowException {
        final Boolean expected = true;

        //given
        when(authenticateRespondentWorkflow.run(AUTH_TOKEN)).thenReturn(expected);

        //when
        Boolean actual = classUnderTest.authenticateRespondent(AUTH_TOKEN);

        //then
        assertEquals(expected, actual);

        verify(authenticateRespondentWorkflow).run(AUTH_TOKEN);
    }

    @Test
    public void givenCaseDataValid_whenSubmit_thenReturnPayload() throws Exception {
        // given
        when(submitToCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        when(submitToCCDWorkflow.errors()).thenReturn(Collections.emptyMap());

        // when
        Map<String, Object> actual = classUnderTest.submit(requestPayload, AUTH_TOKEN);

        // then
        assertEquals(expectedPayload, actual);

        verify(submitToCCDWorkflow).run(requestPayload, AUTH_TOKEN);
        verify(submitToCCDWorkflow).errors();
    }

    @Test
    public void givenCaseDataInvalid_whenSubmit_thenReturnListOfErrors() throws Exception {
        // given
        when(submitToCCDWorkflow.run(requestPayload, AUTH_TOKEN)).thenReturn(expectedPayload);
        Map<String, Object> errors = Collections.singletonMap("new_Error", "An Error");
        when(submitToCCDWorkflow.errors()).thenReturn(errors);

        // when
        Map<String, Object> actual = classUnderTest.submit(requestPayload, AUTH_TOKEN);

        // then
        assertEquals(errors, actual);

        verify(submitToCCDWorkflow).run(requestPayload, AUTH_TOKEN);
        verify(submitToCCDWorkflow, times(2)).errors();
    }

    @Test
    public void givenCaseDataValid_whenUpdate_thenReturnPayload() throws Exception {
        // given
        when(updateToCCDWorkflow.run(requestPayload, AUTH_TOKEN, TEST_CASE_ID))
                .thenReturn(requestPayload);

        // when
        Map<String, Object> actual = classUnderTest.update(requestPayload, AUTH_TOKEN, TEST_CASE_ID);

        // then
        assertEquals(requestPayload, actual);

        verify(updateToCCDWorkflow).run(requestPayload, AUTH_TOKEN, TEST_CASE_ID);
    }

    @After
    public void tearDown() {
        createEventRequest = null;
        requestPayload = null;
        expectedPayload = null;
    }
}