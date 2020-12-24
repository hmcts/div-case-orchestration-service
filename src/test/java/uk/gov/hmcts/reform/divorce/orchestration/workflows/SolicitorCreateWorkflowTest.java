package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddMiniPetitionDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetClaimCostsFromTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetSolicitorCourtDetailsTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCreateWorkflowTest {

    @Mock
    AddMiniPetitionDraftTask addMiniPetitionDraftTask;

    @Mock
    AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @Mock
    SetSolicitorCourtDetailsTask setSolicitorCourtDetailsTask;

    @Mock
    SetClaimCostsFromTask setClaimCostsFromTask;

    @InjectMocks
    SolicitorCreateWorkflow solicitorCreateWorkflow;

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        Map<String, Object> payload = Collections.emptyMap();

        CaseDetails caseDetails = CaseDetails.builder().caseData(payload).build();

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        when(setSolicitorCourtDetailsTask.execute(any(), eq(payload))).thenReturn(payload);
        when(addMiniPetitionDraftTask.execute(any(), eq(payload))).thenReturn(payload);

        assertEquals(payload, solicitorCreateWorkflow.run(caseDetails, AUTH_TOKEN));

        InOrder inOrder = inOrder(setSolicitorCourtDetailsTask, addMiniPetitionDraftTask, addNewDocumentsToCaseDataTask);

        inOrder.verify(setSolicitorCourtDetailsTask).execute(context, payload);
        inOrder.verify(addMiniPetitionDraftTask).execute(context, payload);
        inOrder.verify(addNewDocumentsToCaseDataTask).execute(context, payload);
    }

    @Test
    public void runShouldSetClaimCostsFromWhenClaimCostsIsYesAndClaimCostsFromIsEmpty() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE);

        CaseDetails caseDetails = CaseDetails.builder().caseData(payload).build();

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        when(setClaimCostsFromTask.execute(any(), eq(payload))).thenReturn(payload);
        when(setSolicitorCourtDetailsTask.execute(any(), eq(payload))).thenReturn(payload);
        when(addMiniPetitionDraftTask.execute(any(), eq(payload))).thenReturn(payload);

        assertEquals(Collections.emptyMap(), solicitorCreateWorkflow.run(caseDetails, AUTH_TOKEN));

        InOrder inOrder = inOrder(setClaimCostsFromTask, setSolicitorCourtDetailsTask, addMiniPetitionDraftTask, addNewDocumentsToCaseDataTask);

        inOrder.verify(setClaimCostsFromTask).execute(context, payload);
        inOrder.verify(setSolicitorCourtDetailsTask).execute(context, payload);
        inOrder.verify(addMiniPetitionDraftTask).execute(context, payload);
        inOrder.verify(addNewDocumentsToCaseDataTask).execute(context, payload);
    }
}
