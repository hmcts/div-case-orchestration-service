package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import org.springframework.context.ApplicationEvent;

import java.util.List;

public abstract class AutoPublishingAsyncTask<T> extends AsyncTask<T> {

    @Override
    public T execute(TaskContext context, T payload) throws TaskException {
        getApplicationEventsToPublish(context, payload).forEach(this::publishEvent);

        return payload;
    }

    public abstract List<ApplicationEvent> getApplicationEventsToPublish(TaskContext context, T payload) throws TaskException;

}