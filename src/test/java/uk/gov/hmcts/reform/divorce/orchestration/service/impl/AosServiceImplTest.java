package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendPetitionerAOSOverdueNotificationWorkflow;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class AosServiceImplTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private SendPetitionerAOSOverdueNotificationWorkflow sendPetitionerAOSOverdueNotificationWorkflow;

    @InjectMocks
    private CaseOrchestrationServiceImpl classUnderTest;

    private CcdCallbackRequest ccdCallbackRequest;

    private Map<String, Object> requestPayload;


    @Before
    public void setUp() {
        requestPayload = singletonMap("requestPayloadKey", "requestPayloadValue");
        ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .caseData(requestPayload)
                    .caseId(TEST_CASE_ID)
                    .state(TEST_STATE)
                    .build())
            .eventId(TEST_EVENT_ID)
            .token(TEST_TOKEN)
            .build();
    }

    @Test
    public void givenCaseData_whenSendPetitionerAOSOverdueNotification_thenReturnPayload() throws Exception {
        when(sendPetitionerAOSOverdueNotificationWorkflow.run(ccdCallbackRequest))
                .thenReturn(requestPayload);

        Map<String, Object> actual = classUnderTest.sendPetitionerAOSOverdueNotificationEmail(ccdCallbackRequest);

        assertEquals(requestPayload, actual);

        verify(sendPetitionerAOSOverdueNotificationWorkflow, times(1     )).run(ccdCallbackRequest);
    }

    @Test(expected = WorkflowException.class)
    public void shouldThrowException_ForSendPetitionerAOSOverdueNotification_WhenWorkflowThrowsWorkflowException()
            throws WorkflowException {
        when(sendPetitionerAOSOverdueNotificationWorkflow.run(ccdCallbackRequest))
                .thenThrow(new WorkflowException("This operation threw an exception"));

        classUnderTest.sendPetitionerAOSOverdueNotificationEmail(ccdCallbackRequest);
    }

    @After
    public void tearDown() {
        ccdCallbackRequest = null;
        requestPayload = null;
    }
}
