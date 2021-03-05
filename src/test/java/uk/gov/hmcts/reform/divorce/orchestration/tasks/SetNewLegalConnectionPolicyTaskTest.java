package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_LEGAL_CONNECTION_POLICY_CCD_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SetNewLegalConnectionPolicyTaskTest {

    @InjectMocks
    private SetNewLegalConnectionPolicyTask setNewLegalConnectionPolicyTask;

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {

        testData = new HashMap<>();
        context = new DefaultTaskContext();
    }

    @Test
    public void executeShouldSetNewLegalConnectionPolicyToYes() {
        Map<String, Object> resultData = new HashMap<>();
        resultData.put(NEW_LEGAL_CONNECTION_POLICY_CCD_DATA, YES_VALUE);

        assertEquals(resultData, setNewLegalConnectionPolicyTask.execute(context, testData));
    }
}
