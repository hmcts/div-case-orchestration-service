package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_SERVICE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PERSONAL_SERVICE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_SERVICE_METHOD_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class MigrateCaseToPersonalServiceTaskTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private MigrateCaseToPersonalServiceTask migrateCaseToPersonalServiceTask;

    @Test
    public void shouldChangeSolServiceMethodToPersonalService() {

        TaskContext context = new DefaultTaskContext();

        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        Map<String, Object> payload = new HashMap<>();

        payload.put(SOL_SERVICE_METHOD_CCD_FIELD, COURT_SERVICE_VALUE);

        when(caseMaintenanceClient.updateCase(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(payload);

        Map<String, Object> returnedPayload = migrateCaseToPersonalServiceTask.execute(context, payload);

        assertThat(returnedPayload,
            hasEntry(SOL_SERVICE_METHOD_CCD_FIELD, PERSONAL_SERVICE_VALUE)
        );
    }

}