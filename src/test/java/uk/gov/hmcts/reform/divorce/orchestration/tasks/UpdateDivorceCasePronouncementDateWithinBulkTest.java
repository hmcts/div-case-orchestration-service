package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.springframework.context.ApplicationEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_DETAILS_CONTEXT_KEY;

public class UpdateDivorceCasePronouncementDateWithinBulkTest {

    private UpdateDivorceCasePronouncementDateWithinBulk classToTest  = new UpdateDivorceCasePronouncementDateWithinBulk();

    @Test
    public void givenBulkCase_whenGetEvents_thenReturnBulkPronounceEvent() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(BULK_CASE_DETAILS_CONTEXT_KEY, new HashMap<>());
        Map<String, Object> payload  = Collections.emptyMap();

        List<ApplicationEvent> result = classToTest.getApplicationEvent(context, payload);
        assertEquals(1, result.size());
    }
}
