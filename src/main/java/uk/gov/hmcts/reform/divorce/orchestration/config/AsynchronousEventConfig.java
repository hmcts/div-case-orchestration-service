package uk.gov.hmcts.reform.divorce.orchestration.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsynchronousEventConfig {

    private static final String EVENT_THREAD_NAME = "div-message-executor";

    @Value("${event.thread.pool:10}")
    private int maxThreadPool;

    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster(@Autowired Executor asyncTaskExecutor) {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(asyncTaskExecutor);
        return eventMulticaster;
    }

    @Bean(destroyMethod = "shutdown")
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor asyncTaskExecutor = new ThreadPoolTaskExecutor();
        asyncTaskExecutor.setMaxPoolSize(maxThreadPool);
        asyncTaskExecutor.setThreadGroupName(EVENT_THREAD_NAME);
        asyncTaskExecutor.setThreadNamePrefix(EVENT_THREAD_NAME);
        return asyncTaskExecutor;
    }
}
