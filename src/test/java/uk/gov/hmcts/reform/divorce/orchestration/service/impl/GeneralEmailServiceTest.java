package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class GeneralEmailServiceTest {

    @Mock
    GeneralEmailWorkflow generalEmailWorkflow;

    @InjectMocks
    private GeneralEmailImpl classUnderTest;

    @Test
    public void shouldCallGeneralEmailWorkflow_whenGeneralEmailIsCreated() throws WorkflowException, CaseOrchestrationServiceException {
        Map<String, Object> requestPayload = singletonMap("requestPayloadKey", "requestPayloadValue");
        when(generalEmailWorkflow.run(any()))
            .thenReturn(requestPayload);

        Map<String, Object> actual = classUnderTest.createGeneralEmail(CaseDetails.builder().build());

        assertEquals(requestPayload, actual);
        verify(generalEmailWorkflow).run(CaseDetails.builder().build());
    }

    @Test
    public void shouldCatchWorkflowException_whenGeneralEmailIsCreated() throws WorkflowException {
        when(generalEmailWorkflow.run(any())).thenThrow(WorkflowException.class);

        try {
            classUnderTest.createGeneralEmail(CaseDetails.builder().caseId(TEST_CASE_ID).build());
            fail();
        } catch (CaseOrchestrationServiceException exception) {
            assertThat(exception.getCaseId().isPresent(), is(true));
            assertThat(exception.getCaseId().get(), is(TEST_CASE_ID));
        }
    }
}