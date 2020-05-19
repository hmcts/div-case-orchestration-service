package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.CoRespondentAosAnswersProcessorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.FormFieldValuesToCoreFieldsRelay;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.RespondentAosAnswersProcessorTask;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class AosPackOfflineAnswersWorkflowTest {

    @Mock
    private RespondentAosAnswersProcessorTask respondentAosAnswersProcessor;

    @Mock
    private CoRespondentAosAnswersProcessorTask coRespondentAosAnswersProcessor;

    @Mock
    private FormFieldValuesToCoreFieldsRelay formFieldValuesToCoreFieldsRelay;

    @InjectMocks
    private AosPackOfflineAnswersWorkflow classUnderTest;

    @Test
    public void shouldCallRespondentTask_ForRespondent() throws WorkflowException, TaskException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        when(respondentAosAnswersProcessor.execute(any(), eq(payload))).thenReturn(singletonMap("returnedKey", "returnedValue"));
        when(formFieldValuesToCoreFieldsRelay.execute(any(), eq(payload))).thenReturn(payload);

        Map<String, Object> returnedPayload = classUnderTest.run(payload, RESPONDENT);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));

        verify(formFieldValuesToCoreFieldsRelay, times(1)).execute(any(), eq(payload));
        verify(respondentAosAnswersProcessor, times(1)).execute(any(), eq(payload));
        verify(coRespondentAosAnswersProcessor, never()).execute(any(), eq(payload));
    }

    @Test
    public void shouldCallCoRespondentTask_ForCoRespondent() throws WorkflowException, TaskException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        when(formFieldValuesToCoreFieldsRelay.execute(any(), eq(payload))).thenReturn(payload);
        when(coRespondentAosAnswersProcessor.execute(any(), eq(payload))).thenReturn(singletonMap("returnedKey", "returnedValue"));

        Map<String, Object> returnedPayload = classUnderTest.run(payload, CO_RESPONDENT);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));

        verify(formFieldValuesToCoreFieldsRelay, times(1)).execute(any(), eq(payload));
        verify(respondentAosAnswersProcessor, never()).execute(any(), any());
        verify(coRespondentAosAnswersProcessor, times(1)).execute(any(), eq(payload));
    }

    @Test
    public void shouldCallCoRespondentTask_and_Update_ReceivedAosFromCoResp_to_Yes() throws WorkflowException, TaskException {
        Map<String, Object> caseData = new HashMap<>();

        when(coRespondentAosAnswersProcessor.execute(any(TaskContext.class), anyMap())).thenCallRealMethod();

        Map<String, Object> returnedPayload = classUnderTest.run(caseData, CO_RESPONDENT);

        String coRespValue = (String) returnedPayload.get(RECEIVED_AOS_FROM_CO_RESP);

        assertEquals(RECEIVED_AOS_FROM_CO_RESP + " value should have been set to Yes", YES_VALUE, coRespValue);

        verify(formFieldValuesToCoreFieldsRelay, times(1)).execute(any(TaskContext.class), anyMap());
        verify(respondentAosAnswersProcessor, never()).execute(any(), any());
        verify(coRespondentAosAnswersProcessor, times(1)).execute(any(TaskContext.class), anyMap());
    }

}