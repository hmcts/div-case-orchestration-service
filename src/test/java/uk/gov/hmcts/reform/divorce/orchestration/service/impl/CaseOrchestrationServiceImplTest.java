package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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
    private SubmitToCCDWorkflow submitToCCDWorkflow;

    @Mock
    private CcdCalllbackWorkflow ccdCallbackWorkflow;

    @Mock
    private AuthenticateRespondentWorkflow authenticateRespondentWorkflow;

    @InjectMocks
    private CaseOrchestrationServiceImpl classUnderTest;

    @Test
    public void ccdCallbackHandlerShouldReturnValidCaseDataForValidRequest()
            throws WorkflowException {
        final CreateEvent createEventRequest = createCaseEventRequest();
        final Map<String, Object> expectedPayload = Collections.singletonMap(PIN, TEST_PIN);

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

    private CreateEvent createCaseEventRequest() {
        return CreateEvent.builder()
            .caseDetails(
                CaseDetails.builder()
                    .caseData(new HashMap<>())
                    .caseId(TEST_CASE_ID)
                    .state(TEST_STATE)
                    .build())
            .eventId(TEST_EVENT_ID)
            .token(TEST_TOKEN)
            .build();
    }
}