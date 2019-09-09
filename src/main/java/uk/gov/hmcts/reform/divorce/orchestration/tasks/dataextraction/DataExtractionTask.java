package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequestEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequestEvent.Status;

@Component
public class DataExtractionTask extends AsyncTask<Map<String, Object>> {

    @Override
    public List<ApplicationEvent> getApplicationEvent(TaskContext context, Map<String, Object> payload) {

        DataExtractionRequestEvent aosDataExtractionEvent =
                new DataExtractionRequestEvent(context, Status.AOS, yesterday());

        DataExtractionRequestEvent dnDataExtractionEvent =
                new DataExtractionRequestEvent(context, Status.DN, yesterday());

        DataExtractionRequestEvent daDataExtractionEvent =
                new DataExtractionRequestEvent(context, Status.DA, yesterday());

        List<ApplicationEvent> dataExtractionRequestEvents = new LinkedList<>();
        dataExtractionRequestEvents.add(aosDataExtractionEvent);
        dataExtractionRequestEvents.add(dnDataExtractionEvent);
        dataExtractionRequestEvents.add(daDataExtractionEvent);

        return dataExtractionRequestEvents;
    }

    private LocalDate yesterday() {
        return LocalDate.now().minusDays(1);
    }

}