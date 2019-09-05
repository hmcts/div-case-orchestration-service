package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitRespondentAosCaseForSolicitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorSubmitsAosWorkflowTest {

    @Mock
    SubmitRespondentAosCaseForSolicitor submitRespondentAosCaseForSolicitor;

    @InjectMocks
    SolicitorSubmitsAosWorkflow solicitorSubmitsAosWorkflow;

    @Test
    public void whenSolicitorIsNotRepresentingResp_shouldNotExecuteTaskAndReturnPayload() throws Exception {
        Map<String, Object> payload = Collections.emptyMap();

        CaseDetails caseDetails = CaseDetails.builder().caseData(payload).build();

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        solicitorSubmitsAosWorkflow.run(caseDetails, AUTH_TOKEN);

        verifyZeroInteractions(submitRespondentAosCaseForSolicitor);
    }

    @Test
    public void whenSolicitorIsRepresentingResp_shouldExecuteTaskAndReturnPayload() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put(RESP_SOL_REPRESENTED, YES_VALUE);

        CaseDetails caseDetails = CaseDetails.builder().caseData(payload).build();

        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        when(submitRespondentAosCaseForSolicitor.execute(context, payload)).thenReturn(payload);

        assertEquals(payload, solicitorSubmitsAosWorkflow.run(caseDetails, AUTH_TOKEN));
    }
}
