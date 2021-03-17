package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class UpdateRespondentDigitalDetailsTaskTest {

    @InjectMocks
    private UpdateRespondentDigitalDetailsTask target;

    @Test
    public void updateRespondentDigitalDetailsTaskTest() {
        final TaskContext context = new DefaultTaskContext();
        Map<String, Object> caseData = new HashMap();

        target.execute(context, caseData);

        assertEquals(caseData.get(RESP_IS_USING_DIGITAL_CHANNEL), YES_VALUE);
        assertEquals(caseData.get(RESP_SOL_REPRESENTED), YES_VALUE);
    }
}