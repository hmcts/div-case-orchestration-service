package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddDecreeNisiGrantedDateToPayloadTask;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class DecreeNisiAboutToBeGrantedWorkflowTest {

    private static final String DECREE_NISI_GRANTED_CCD_FIELD = "DecreeNisiGranted";

    @Mock
    private AddDecreeNisiGrantedDateToPayloadTask addDecreeNisiGrantedDateToPayloadTask;

    @InjectMocks
    private DecreeNisiAboutToBeGrantedWorkflow workflow;

    @Test
    public void shouldCallTasksAccordingly_IfDecreeNisiIsGranted() throws WorkflowException {
        Map<String, Object> inputPayload = singletonMap(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE);
        Map<String, Object> payloadReturnedByTask = singletonMap("returnedKey", "returnedValue");
        when(addDecreeNisiGrantedDateToPayloadTask.execute(isNotNull(), eq(inputPayload))).thenReturn(payloadReturnedByTask);

        Map<String, Object> returnedPayload = workflow.run(CaseDetails.builder().caseData(inputPayload).build());

        assertThat(returnedPayload, equalTo(payloadReturnedByTask));
        verify(addDecreeNisiGrantedDateToPayloadTask).execute(isNotNull(), eq(inputPayload));
    }

    @Test
    public void shouldCallTasksAccordingly_IfDecreeNisiIsNotGranted() throws WorkflowException {
        Map<String, Object> inputPayload = singletonMap(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);

        workflow.run(CaseDetails.builder().caseData(inputPayload).build());

        verify(addDecreeNisiGrantedDateToPayloadTask, never()).execute(any(), any());
    }

}