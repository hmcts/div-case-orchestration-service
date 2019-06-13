package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.springframework.context.ApplicationEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class UpdateDivorceCaseHearingDetailsWithinBulkTest {

    private UpdateDivorceCaseHearingDetailsWithinBulk classToTest  = new UpdateDivorceCaseHearingDetailsWithinBulk();

    @Test
    public void givenBulkCase_whenGetEvents_thenReturnBulkScheduleEvent() {
        TaskContext context = new DefaultTaskContext();
        Map<String, Object> payload  = Collections.emptyMap();

        List<ApplicationEvent> result = classToTest.getApplicationEvent(context, payload);
        assertEquals(1, result.size());
    }
}
