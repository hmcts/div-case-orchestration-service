package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddDecreeNisiGrantedDateToPayloadTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class DecreeNisiAboutToBeGrantedWorkflowTest {

    private static final String DECREE_NISI_GRANTED_CCD_FIELD = "DecreeNisiGranted";
    private static final String STATE_CCD_FIELD = "state";

    @Mock
    private AddDecreeNisiGrantedDateToPayloadTask addDecreeNisiGrantedDateToPayloadTask;

    @InjectMocks
    private DecreeNisiAboutToBeGrantedWorkflow workflow;

    private Map<String, Object> inputPayload;

    @Before
    public void setUp() {
        inputPayload = new HashMap<>();
    }

    @Test
    public void shouldCallTasksAccordingly_IfDecreeNisiIsGranted() throws WorkflowException {
        inputPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE);

        Map<String, Object> payloadReturnedByTask = new HashMap<>(inputPayload);
        payloadReturnedByTask.put("addedKey", "addedValue");
        when(addDecreeNisiGrantedDateToPayloadTask.execute(isNotNull(), eq(inputPayload))).thenReturn(payloadReturnedByTask);

        Map<String, Object> returnedPayload = workflow.run(CaseDetails.builder().caseData(inputPayload).build());

        assertThat(returnedPayload, allOf(
            hasEntry(equalTo("addedKey"), equalTo("addedValue")),
            hasEntry(equalTo(DECREE_NISI_GRANTED_CCD_FIELD), equalTo(YES_VALUE)),
            hasEntry(equalTo(STATE_CCD_FIELD), equalTo(AWAITING_PRONOUNCEMENT))
        ));
        verify(addDecreeNisiGrantedDateToPayloadTask).execute(isNotNull(), eq(inputPayload));
    }

    @Test
    public void shouldCallTasksAccordingly_IfDecreeNisiIsNotGranted() throws WorkflowException {
        inputPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);

        Map<String, Object> returnedPayload = workflow.run(CaseDetails.builder().caseData(inputPayload).build());

        assertThat(returnedPayload, allOf(
            hasEntry(equalTo(DECREE_NISI_GRANTED_CCD_FIELD), equalTo(NO_VALUE)),
            hasEntry(equalTo(STATE_CCD_FIELD), equalTo(AWAITING_CLARIFICATION))
        ));
        verify(addDecreeNisiGrantedDateToPayloadTask, never()).execute(any(), any());
    }

}