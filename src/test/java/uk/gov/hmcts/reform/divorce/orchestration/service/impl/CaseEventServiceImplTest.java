package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.CleanStatusEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CleanStateFromCaseDataWorkflow;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class CaseEventServiceImplTest {

    @Mock
    private CleanStateFromCaseDataWorkflow cleanStateFromCaseDataWorkflow;

    @InjectMocks
    private CaseEventServiceImpl classUnderTest;

    @Test
    public void shouldCallCleanStatusWorkflow() throws WorkflowException {
        DefaultTaskContext contextTask = new DefaultTaskContext();
        contextTask.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        contextTask.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        CleanStatusEvent event = new CleanStatusEvent(contextTask);

        when(cleanStateFromCaseDataWorkflow.run(TEST_CASE_ID, AUTH_TOKEN)).thenReturn(singletonMap("returnedKey", "returnedValue"));

        Map<String, Object> returnedPayload = classUnderTest.cleanStateFromData(event);

        verify(cleanStateFromCaseDataWorkflow).run(TEST_CASE_ID, AUTH_TOKEN);
        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));
    }
}
