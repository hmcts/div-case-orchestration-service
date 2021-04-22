package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.exception.JudgeServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.JudgeCostsDecisionWorkflow;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class JudgeServiceImplTest {

    @Mock
    private JudgeCostsDecisionWorkflow judgeCostsDecisionWorkflow;

    @InjectMocks
    private JudgeServiceImpl classUnderTest;

    private CcdCallbackRequest ccdCallbackRequest;

    private Map<String, Object> requestPayload;

    private Map<String, Object> expectedPayload;

    @Before
    public void setUp() {
        requestPayload = singletonMap("requestPayloadKey", "requestPayloadValue");
        ccdCallbackRequest = buildCcdCallbackRequest(requestPayload);
    }

    @Test
    public void shouldCallRightWorkflow_WhenJudgeCostsDecision()
            throws JudgeServiceException, WorkflowException {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        when(judgeCostsDecisionWorkflow.run(any())).thenReturn(expectedPayload);

        classUnderTest.judgeCostsDecision(ccdCallbackRequest);

        verify(judgeCostsDecisionWorkflow).run(caseDetails);
    }

    private CcdCallbackRequest buildCcdCallbackRequest(Map<String, Object> requestPayload) {
        return CcdCallbackRequest.builder()
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

    @After
    public void tearDown() {
        ccdCallbackRequest = null;
        requestPayload = null;
        expectedPayload = null;
    }
}
