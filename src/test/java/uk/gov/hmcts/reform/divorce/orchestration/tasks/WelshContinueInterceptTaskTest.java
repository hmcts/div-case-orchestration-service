package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_PREVIOUS_STATE;

@RunWith(MockitoJUnitRunner.class)
public class WelshContinueInterceptTaskTest {
    @InjectMocks
    private WelshContinueInterceptTask welshContinueInterceptTask;

    @Test
    public void testNextStateSet() throws TaskException {
        Map<String, Object> payLoad = new HashMap<>();
        payLoad.put( WELSH_PREVIOUS_STATE, "previousState" );

        Map<String, Object> result = welshContinueInterceptTask.execute(new DefaultTaskContext(), payLoad);

        assertThat(result.get(STATE_CCD_FIELD), CoreMatchers.equalTo(payLoad.get(WELSH_PREVIOUS_STATE)));
    }

    @Test
    public void testNextStateEmpty() throws TaskException {
        Map<String, Object> payLoad = new HashMap<>();

        Map<String, Object> result = welshContinueInterceptTask.execute(new DefaultTaskContext(), payLoad);

        assertThat(result.get(STATE_CCD_FIELD), CoreMatchers.nullValue());
    }


}
