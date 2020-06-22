package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.CoRespondentAosAnswersProcessorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.CoRespondentAosDerivedAddressFormatterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.FormFieldValuesToCoreFieldsRelayTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.RespondentAosAnswersProcessorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.RespondentAosDerivedAddressFormatterTask;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
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

    @Mock
    private RespondentAosDerivedAddressFormatterTask respondentAosDerivedAddressFormatter;

    @InjectMocks
    private AosPackOfflineAnswersWorkflow classUnderTest;

    @Test
    public void shouldCallRespondentTask_ForRespondent() throws WorkflowException, TaskException {

        Map<String, Object> payload = singletonMap("testKey", "testValue");
        Map<String, Object> returnCaseData = singletonMap("returnedKey", "returnedValue");
        CaseDetails caseDetails = buildCaseDetail(payload);

        when(respondentAosAnswersProcessor.execute(any(TaskContext.class), eq(payload))).thenReturn(returnCaseData);
        when(formFieldValuesToCoreFieldsRelay.execute(any(TaskContext.class), eq(payload))).thenReturn(payload);
        when(respondentAosDerivedAddressFormatter.execute(any(TaskContext.class), anyMap())).thenReturn(returnCaseData);

        Map<String, Object> returnedPayload = classUnderTest.run(caseDetails, RESPONDENT);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));

        InOrder inOrder = inOrder(formFieldValuesToCoreFieldsRelay, respondentAosAnswersProcessor, respondentAosDerivedAddressFormatter);
        inOrder.verify(formFieldValuesToCoreFieldsRelay).execute(any(), eq(payload));
        inOrder.verify(respondentAosAnswersProcessor).execute(any(), eq(payload));
        inOrder.verify(respondentAosDerivedAddressFormatter).execute(any(), anyMap());

        verify(coRespondentAosAnswersProcessor, never()).execute(any(), eq(payload));
    }

    @Test
    public void shouldCallCoRespondentTask_ForCoRespondent() throws WorkflowException, TaskException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        Map<String, Object> returnedCaseData = singletonMap("returnedKey", "returnedValue");
        CaseDetails caseDetails = buildCaseDetail(payload);

        when(formFieldValuesToCoreFieldsRelay.execute(any(), eq(payload))).thenReturn(payload);
        when(coRespondentAosAnswersProcessor.execute(any(), eq(payload))).thenReturn(returnedCaseData);
        when(coRespondentAosDerivedAddressFormatter.execute(any(), anyMap())).thenReturn(returnedCaseData);

        Map<String, Object> returnedPayload = classUnderTest.run(caseDetails, CO_RESPONDENT);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));

        InOrder inOrder = inOrder(formFieldValuesToCoreFieldsRelay, coRespondentAosAnswersProcessor, coRespondentAosDerivedAddressFormatter);
        inOrder.verify(formFieldValuesToCoreFieldsRelay).execute(any(), eq(payload));
        inOrder.verify(coRespondentAosAnswersProcessor).execute(any(), eq(payload));
        inOrder.verify(coRespondentAosDerivedAddressFormatter).execute(any(), anyMap());

        verify(respondentAosAnswersProcessor, never()).execute(any(), any());
    }

    @Test
    public void shouldCallCoRespondentTask_and_Update_ReceivedAosFromCoResp_to_Yes() throws WorkflowException, TaskException {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> returnedCaseData = singletonMap("returnedKey", "returnedValue");
        CaseDetails caseDetails = buildCaseDetail(payload);

        when(coRespondentAosAnswersProcessor.execute(any(), anyMap())).thenReturn(payload);
        when(coRespondentAosDerivedAddressFormatter.execute(any(), anyMap())).thenReturn(returnedCaseData);

        Map<String, Object> returnedPayload = classUnderTest.run(caseDetails, CO_RESPONDENT);

        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));

        InOrder inOrder = inOrder(formFieldValuesToCoreFieldsRelay, coRespondentAosAnswersProcessor, coRespondentAosDerivedAddressFormatter);
        inOrder.verify(formFieldValuesToCoreFieldsRelay).execute(any(), anyMap());
        inOrder.verify(coRespondentAosAnswersProcessor).execute(any(), anyMap());
        inOrder.verify(coRespondentAosDerivedAddressFormatter).execute(any(), anyMap());

        verify(respondentAosAnswersProcessor, never()).execute(any(), anyMap());
    }

    private CaseDetails buildCaseDetail(Map<String, Object> payload) {
        return CaseDetails.builder().caseId(TEST_CASE_ID).caseData(payload).build();
    }

}