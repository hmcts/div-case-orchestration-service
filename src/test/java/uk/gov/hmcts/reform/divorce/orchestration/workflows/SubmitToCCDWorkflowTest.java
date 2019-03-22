package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CourtAllocationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DeleteDraft;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DuplicateCaseValidationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.REASON_FOR_DIVORCE_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SubmitToCCDWorkflowTest {

    @Mock
    private CourtAllocationTask courtAllocationTask;

    @Mock
    private FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    @Mock
    private ValidateCaseData validateCaseData;

    @Mock
    private SubmitCaseToCCD submitCaseToCCD;

    @Mock
    private DeleteDraft deleteDraft;

    @Mock
    private DuplicateCaseValidationTask duplicateCaseValidationTask;

    @InjectMocks
    private SubmitToCCDWorkflow submitToCCDWorkflow;

    @Test
    public void runShouldExecuteTasks_AddCourtToContext_AndReturnPayloadWithAllocatedCourt() throws Exception {
        Map<String, Object> incomingPayload = singletonMap(REASON_FOR_DIVORCE_KEY, "adultery");
        when(courtAllocationTask.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);
        when(duplicateCaseValidationTask.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);
        when(formatDivorceSessionToCaseData.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);
        when(validateCaseData.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);
        when(submitCaseToCCD.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);
        when(deleteDraft.execute(any(), eq(incomingPayload))).thenReturn(incomingPayload);

        Map<String, Object> actual = submitToCCDWorkflow.run(incomingPayload, AUTH_TOKEN);

        assertEquals(actual, incomingPayload);
        verify(duplicateCaseValidationTask).execute(any(), eq(incomingPayload));
        verify(courtAllocationTask).execute(any(), eq(incomingPayload));
        verify(formatDivorceSessionToCaseData).execute(any(), eq(incomingPayload));
        verify(validateCaseData).execute(any(), eq(incomingPayload));
        verify(submitCaseToCCD).execute(any(), eq(incomingPayload));
        verify(deleteDraft).execute(any(), eq(incomingPayload));
    }
}