package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_NEXT_EVENT;

@RunWith(MockitoJUnitRunner.class)
public class WelshContinueTaskTest {
    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private WelshContinueTask welshContinueTask;

    private TaskContext context;
    private Map<String, Object> caseData;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        caseData = new HashMap<>();
        caseData.put(WELSH_NEXT_EVENT, "Continue");
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, "KEY");
        context.setTransientObject(CASE_ID_JSON_KEY, "CASEID");
    }

    @Test
    public void testExecuteSuccess() throws TaskException {
        when(caseMaintenanceClient.updateCase(context.getTransientObject(AUTH_TOKEN_JSON_KEY),
            context.getTransientObject(CASE_ID_JSON_KEY), (String) caseData.get(WELSH_NEXT_EVENT), caseData))
            .thenReturn(Collections.EMPTY_MAP);
        welshContinueTask.execute(context, caseData);
        caseData.put(WELSH_NEXT_EVENT, null);
        verify(caseMaintenanceClient).updateCase(eq(context.getTransientObject(AUTH_TOKEN_JSON_KEY)),
            eq(context.getTransientObject(CASE_ID_JSON_KEY)),same("Continue"),
            eq(caseData));
        assertThat(caseData).containsEntry(WELSH_NEXT_EVENT, null);
    }

    @Test
    public void testExecuteFailure()  {
        Request request = Request.create(Request.HttpMethod.PATCH, "http://localhost:8080", Collections.EMPTY_MAP, Request.Body.empty(), null);
        Response response = Response.builder().request(request).status(422).reason("Unprocessable Entity ").build();
        FeignException exception = FeignException.errorStatus("update", response);
        when(caseMaintenanceClient.updateCase(context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                context.getTransientObject(CASE_ID_JSON_KEY), (String) caseData.get(WELSH_NEXT_EVENT), caseData))
               .thenThrow(exception);

        try {
            welshContinueTask.execute(context, caseData);
        } catch (TaskException e) {
            assertEquals("For case: CASEID update failed for event id Continue", e.getMessage());
        }
        verify(caseMaintenanceClient).updateCase(eq(context.getTransientObject(AUTH_TOKEN_JSON_KEY)),
                eq(context.getTransientObject(CASE_ID_JSON_KEY)),same("Continue"),
                eq(caseData));
        assertNotNull("WELSH_NEXT_EVENT should be present ",caseData.get(WELSH_NEXT_EVENT));
    }

    @Test
    public void testExecuteWelsh_Next_Event_Not_Present() throws TaskException {
        caseData.remove(WELSH_NEXT_EVENT);

        welshContinueTask.execute(context, caseData);
        verify(caseMaintenanceClient, never()).updateCase(eq(context.getTransientObject(AUTH_TOKEN_JSON_KEY)),
                eq(context.getTransientObject(CASE_ID_JSON_KEY)),isNull(),
                eq(caseData));
    }
}