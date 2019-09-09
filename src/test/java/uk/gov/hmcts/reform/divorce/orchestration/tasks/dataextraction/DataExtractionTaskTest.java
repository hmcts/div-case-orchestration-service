package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.junit.Test;
import org.springframework.context.ApplicationEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DataExtractionTaskTest {

    private DataExtractionTask classUnderTest = new DataExtractionTask();

    @Test
    public void givenDataExtraction_whenGetEvents_thenReturnCorrectNumberOfEvents() {
        TaskContext context = new DefaultTaskContext();
        Map<String, Object> payload = new HashMap<>();
        int expectedNumberOfEvents = 3;
        List<ApplicationEvent> result = classUnderTest.getApplicationEvent(context, payload);
        assertEquals(expectedNumberOfEvents, result.size());
    }
}
