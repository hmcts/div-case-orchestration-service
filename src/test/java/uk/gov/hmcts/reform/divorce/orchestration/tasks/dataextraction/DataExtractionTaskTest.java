package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.junit.Test;
import org.springframework.context.ApplicationEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequestEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequestEvent.Status.AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequestEvent.Status.DA;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequestEvent.Status.DN;

public class DataExtractionTaskTest {

    private final LocalDate yesterday = LocalDate.now().minusDays(1);

    private DataExtractionTask classUnderTest = new DataExtractionTask();

    @Test
    public void givenDataExtraction_whenGetEvents_thenReturnCorrectNumberOfEvents() {
        TaskContext context = new DefaultTaskContext();
        Map<String, Object> payload = new HashMap<>();
        List<ApplicationEvent> events = classUnderTest.getApplicationEvent(context, payload);

        assertThat(events, hasSize(3));
        assertEventIsAsExpected((DataExtractionRequestEvent) events.get(0), yesterday, AOS);
        assertEventIsAsExpected((DataExtractionRequestEvent) events.get(1), yesterday, DN);
        assertEventIsAsExpected((DataExtractionRequestEvent) events.get(2), yesterday, DA);
        //TODO - then unify classes

        //TODO - are there functional tests
    }

    private void assertEventIsAsExpected(DataExtractionRequestEvent eventToAssert, LocalDate expectedDate, DataExtractionRequestEvent.Status expectedStatus) {
        assertThat(eventToAssert.getDate(), equalTo(expectedDate));
        assertThat(eventToAssert.getStatus(), equalTo(expectedStatus));
    }

}