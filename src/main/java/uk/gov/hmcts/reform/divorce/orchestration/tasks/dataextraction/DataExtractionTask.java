package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DA;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DN;

@Component
@Slf4j
public class DataExtractionTask extends AsyncTask<Map<String, Object>> {

    @Override
    public List<ApplicationEvent> getApplicationEvent(TaskContext context, Map<String, Object> payload) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Request data extractions for {}", yesterday);

        DataExtractionRequest aosDataExtractionEvent = new DataExtractionRequest(context, AOS, yesterday);
        DataExtractionRequest dnDataExtractionEvent = new DataExtractionRequest(context, DN, yesterday);
        DataExtractionRequest daDataExtractionEvent = new DataExtractionRequest(context, DA, yesterday);

        List<ApplicationEvent> dataExtractionRequestEvents = new LinkedList<>();
        dataExtractionRequestEvents.add(aosDataExtractionEvent);
        dataExtractionRequestEvents.add(dnDataExtractionEvent);
        dataExtractionRequestEvents.add(daDataExtractionEvent);

        return dataExtractionRequestEvents;
    }

}