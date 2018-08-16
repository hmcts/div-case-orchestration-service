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
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;

@RunWith(MockitoJUnitRunner.class)
public class CaseOrchestrationServiceImplTest {
    public static final String TEST_CASE_ID = "test.case.id";
    public static final String TEST_STATE = "test.state";
    public static final String TEST_PIN = "abcd1234";
    private static String TEST_TOKEN = "test.token";
    private static String TEST_EVENT_ID = "test.event.id";
    private static String AUTH_TOKEN = "test.auth.token";
    @Mock
    private SubmitToCCDWorkflow submitToCCDWorkflow;

    @Mock
    private CcdCalllbackWorkflow ccdCallbackWorkflow;

    @InjectMocks
    private CaseOrchestrationServiceImpl service;

    private CreateEvent createEventRequest;

    private Map<String, Object> expectedPayload;


    @Before
    public void setUp(){
        createEventRequest = CreateEvent.builder().caseDetails(CaseDetails.builder().caseData(new HashMap<>()).caseId(TEST_CASE_ID).state(TEST_STATE).build()).eventId(TEST_EVENT_ID).token(TEST_TOKEN).build();
        expectedPayload = new HashMap<>();
        expectedPayload.put(PIN, TEST_PIN);
    }

    @Test
    public void ccdCallbackHandlerShouldReturnValidCaseDataForValidRequest() throws WorkflowException {
        //given
        when(ccdCallbackWorkflow.run(createEventRequest, AUTH_TOKEN)).thenReturn(expectedPayload);

        //when
        Map<String, Object> actual = service.ccdCallbackHandler(createEventRequest, AUTH_TOKEN);

        //then
        assertEquals(expectedPayload,actual);
        assertEquals(expectedPayload.get(PIN), TEST_PIN);
    }

    @After
    public void tearDown(){
        createEventRequest = null;
        expectedPayload = null;
    }

}