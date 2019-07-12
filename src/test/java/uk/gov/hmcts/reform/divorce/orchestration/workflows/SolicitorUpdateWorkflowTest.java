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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorUpdateWorkflowTest {

    @Mock
    AddMiniPetitionDraftTask addMiniPetitionDraftTask;

    @Mock
    CaseFormatterAddDocuments caseFormatterAddDocuments;

    @InjectMocks
    SolicitorUpdateWorkflow solicitorUpdateWorkflow;

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        Map<String, Object> payload = Collections.emptyMap();

        CaseDetails caseDetails = CaseDetails.builder().caseData(payload).build();

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        when(addMiniPetitionDraftTask.execute(context, payload)).thenReturn(payload);

        assertEquals(payload, solicitorUpdateWorkflow.run(caseDetails, AUTH_TOKEN));

        InOrder inOrder = inOrder(addMiniPetitionDraftTask, caseFormatterAddDocuments);

        inOrder.verify(addMiniPetitionDraftTask).execute(context, payload);
        inOrder.verify(caseFormatterAddDocuments).execute(context, payload);
    }
}
