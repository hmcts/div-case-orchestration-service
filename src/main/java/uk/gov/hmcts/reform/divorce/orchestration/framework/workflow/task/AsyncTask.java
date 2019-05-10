package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

public abstract class AsyncTask<T> implements Task<T> {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public abstract List<ApplicationEvent> getApplicationEvent(TaskContext context, T payload) throws TaskException;

    @Override
    public T execute(TaskContext context, T payload) throws TaskException {
        getApplicationEvent(context, payload).forEach( applicationEventPublisher::publishEvent );

        return payload;
    }
}
