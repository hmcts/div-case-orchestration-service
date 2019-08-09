package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline.IssueAosPackOfflineWorkflow;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AosPackOfflineServiceImplTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private IssueAosPackOfflineWorkflow workflow;

    @InjectMocks
    private AosPackOfflineServiceImpl aosPackOfflineService;

    @Test
    public void testWorkflowIsCalledWithRightParams() throws WorkflowException, CaseOrchestrationServiceException {
        String testAuthToken = "testAuthToken";
        CaseDetails caseDetails = CaseDetails.builder().build();
        Map<String, Object> returnValue = singletonMap("returnedKey", "returnedValue");
        when(workflow.run(any(), any(), any())).thenReturn(returnValue);

        Map<String, Object> result = aosPackOfflineService.issueAosPackOffline(testAuthToken, caseDetails, DivorceParty.CO_RESPONDENT);

        verify(workflow).run(eq(testAuthToken), eq(caseDetails), eq(DivorceParty.CO_RESPONDENT));
        assertThat(result, equalTo(returnValue));
    }

    @Test
    public void shouldThrowServiceException() throws WorkflowException, CaseOrchestrationServiceException {
        when(workflow.run(any(), any(), any())).thenThrow(WorkflowException.class);
        expectedException.expect(CaseOrchestrationServiceException.class);
        expectedException.expectCause(instanceOf(WorkflowException.class));

        CaseDetails caseDetails = CaseDetails.builder().caseId("123456789").build();
        aosPackOfflineService.issueAosPackOffline(null, caseDetails, DivorceParty.RESPONDENT);
    }

}