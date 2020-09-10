package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.GeneralEmailWorkflow;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeneralEmailServiceTest {

    @Mock
    GeneralEmailWorkflow generalEmailWorkflow;

    @InjectMocks
    private GeneralEmailImpl classUnderTest;

    private Map<String, Object> requestPayload;

    @Before
    public void setUp() {
        requestPayload = singletonMap("requestPayloadKey", "requestPayloadValue");
    }

    @Test
    public void shouldCallGeneralEmailWorkflow_whenGeneralEmailIsCreated() throws WorkflowException, CaseOrchestrationServiceException {
        when(generalEmailWorkflow.run(any()))
            .thenReturn(requestPayload);

        Map<String, Object> actual = classUnderTest.createGeneralEmail(CaseDetails.builder().build());

        assertEquals(requestPayload, actual);
        verify(generalEmailWorkflow).run(CaseDetails.builder().build());
    }

    @Test
    public void shouldCatchWorkflowException_whenGeneralEmailIsCreated() throws WorkflowException {
        when(generalEmailWorkflow.run(any())).thenThrow(WorkflowException.class);
    }

    @After
    public void tearDown() {
        requestPayload = null;
    }
}