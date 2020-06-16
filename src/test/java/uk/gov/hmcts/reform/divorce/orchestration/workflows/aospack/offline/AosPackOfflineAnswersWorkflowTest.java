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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.CoRespondentAosDerivedAddressFormatterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.FormFieldValuesToCoreFieldsRelayTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.RespondentAosAnswersProcessorTask;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class AosPackOfflineAnswersWorkflowTest {

    @Mock
    private RespondentAosAnswersProcessorTask respondentAosAnswersProcessor;

    @Mock
    private CoRespondentAosAnswersProcessorTask coRespondentAosAnswersProcessor;

    @Mock
    private FormFieldValuesToCoreFieldsRelayTask formFieldValuesToCoreFieldsRelay;

    @Mock
    private CoRespondentAosDerivedAddressFormatterTask coRespondentAosDerivedAddressFormatter;

    @InjectMocks
    private AosPackOfflineAnswersWorkflow classUnderTest;

    @Test
    public void shouldCallRespondentTask_ForRespondent() throws WorkflowException, TaskException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        Map<String, Object> returnCaseData = singletonMap("returnedKey", "returnedValue");

        when(respondentAosAnswersProcessor.execute(any(TaskContext.class), eq(payload))).thenReturn(returnCaseData);
        when(formFieldValuesToCoreFieldsRelay.execute(any(TaskContext.class), eq(payload))).thenReturn(payload);
        when(coRespondentAosDerivedAddressFormatter.execute(any(TaskContext.class), anyMap())).thenReturn(returnCaseData);

        Map<String, Object> returnedPayload = classUnderTest.run(payload, RESPONDENT);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));

        // TODO use inorder
        verify(formFieldValuesToCoreFieldsRelay, times(1)).execute(any(), eq(payload));
        verify(respondentAosAnswersProcessor, times(1)).execute(any(), eq(payload));
        verify(coRespondentAosAnswersProcessor, never()).execute(any(), eq(payload));
        verify(coRespondentAosDerivedAddressFormatter, times(1)).execute(any(), anyMap());
    }

    @Test
    public void shouldCallCoRespondentTask_ForCoRespondent() throws WorkflowException, TaskException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        Map<String, Object> returnedCaseData = singletonMap("returnedKey", "returnedValue");

        when(formFieldValuesToCoreFieldsRelay.execute(any(), eq(payload))).thenReturn(payload);
        when(coRespondentAosAnswersProcessor.execute(any(), eq(payload))).thenReturn(returnedCaseData);
        when(coRespondentAosDerivedAddressFormatter.execute(any(), anyMap())).thenReturn(returnedCaseData);


        Map<String, Object> returnedPayload = classUnderTest.run(payload, CO_RESPONDENT);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));

        // TODO use inorder
        verify(formFieldValuesToCoreFieldsRelay, times(1)).execute(any(), eq(payload));
        verify(respondentAosAnswersProcessor, never()).execute(any(), any());
        verify(coRespondentAosAnswersProcessor, times(1)).execute(any(), eq(payload));
        verify(coRespondentAosDerivedAddressFormatter, times(1)).execute(any(), anyMap());
    }

    @Test
    public void shouldCallCoRespondentTask_and_Update_ReceivedAosFromCoResp_to_Yes() throws WorkflowException, TaskException {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> returnedCaseData = singletonMap("returnedKey", "returnedValue");

        when(coRespondentAosAnswersProcessor.execute(any(), anyMap())).thenReturn(payload);
        when(coRespondentAosDerivedAddressFormatter.execute(any(), anyMap())).thenReturn(returnedCaseData);

        Map<String, Object> returnedPayload = classUnderTest.run(payload, CO_RESPONDENT);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));


        // TODO use inorder
        verify(formFieldValuesToCoreFieldsRelay, times(1)).execute(any(), anyMap());
        verify(respondentAosAnswersProcessor, never()).execute(any(), anyMap());
        verify(coRespondentAosAnswersProcessor, times(1)).execute(any(), anyMap());
        verify(coRespondentAosDerivedAddressFormatter, times(1)).execute(any(), anyMap());
    }

}