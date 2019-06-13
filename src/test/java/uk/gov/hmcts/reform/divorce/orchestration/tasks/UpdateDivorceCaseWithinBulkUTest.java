package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.context.ApplicationEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_LIST_KEY;

public class UpdateDivorceCaseWithinBulkUTest {


    private UpdateDivorceCaseWithinBulk classToTest  = new UpdateDivorceCaseWithinBulk();

    @Test
    public void givenEmptyMap_whenGetEvents_thenReturnEmptyList() {
        TaskContext context = new DefaultTaskContext();
        Map<String, Object> payload  = Collections.emptyMap();

        assertEquals(Collections.emptyList(), classToTest.getApplicationEvent(context, payload));
    }

    @Test
    public void givenListCase_thenReturnEventList() {
        TaskContext context = new DefaultTaskContext();
        Map<String, Object> caseData = ImmutableMap.of("someKey", "someData");

        Map<String, Object> payload  = ImmutableMap.of(BULK_CASE_LIST_KEY, Arrays.asList(caseData, caseData));

        List<ApplicationEvent> result = classToTest.getApplicationEvent(context, payload);
        assertEquals(2, result.size());
    }
}
