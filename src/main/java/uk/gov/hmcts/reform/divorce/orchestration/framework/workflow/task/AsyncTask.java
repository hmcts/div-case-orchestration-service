package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

public abstract class AsyncTask<T> implements Task<T> {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    void publishEvent(ApplicationEvent eventToPublish) {
        applicationEventPublisher.publishEvent(eventToPublish);
    }

}