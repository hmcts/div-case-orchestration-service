package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import org.springframework.context.ApplicationEvent;

import java.util.function.Consumer;

public abstract class SelfPublishingAsyncTask<T> extends AsyncTask<T> {

    @Override
    public T execute(TaskContext context, T payload) throws TaskException {
        publishApplicationEvents(context, payload, this::publishEvent);

        return payload;
    }

    protected abstract void publishApplicationEvents(TaskContext context, T payload, Consumer<? super ApplicationEvent> eventPublishingFunction);

}