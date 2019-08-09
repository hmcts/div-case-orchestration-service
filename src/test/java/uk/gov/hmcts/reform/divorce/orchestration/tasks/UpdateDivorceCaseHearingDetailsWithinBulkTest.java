package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;


public class UpdateDivorceCaseHearingDetailsWithinBulkTest {


    private UpdateDivorceCaseHearingDetailsWithinBulk classToTest;

    @Before
    public void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        classToTest  = new UpdateDivorceCaseHearingDetailsWithinBulk(objectMapper);
    }

    @Test
    public void givenBulkCase_whenGetEvents_thenReturnBulkScheduleEvent() {
        TaskContext context = new DefaultTaskContext();
        Map<String, Object> payload  = Collections.emptyMap();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, payload);
        List<ApplicationEvent> result = classToTest.getApplicationEvent(context, payload);
        assertEquals(1, result.size());
    }
}
