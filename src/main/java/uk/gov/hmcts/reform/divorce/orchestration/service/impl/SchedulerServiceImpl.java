package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.config.SchedulerConfig;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.AbstractJobSchedulerTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.SchedulerService;
import uk.gov.hmcts.reform.divorce.scheduler.services.JobService;

import javax.annotation.PostConstruct;

/**
 * Schedule service implementation.
 * @author  Ganesh Raja
 */
@EnableConfigurationProperties(SchedulerConfig.class)
@Component
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {

    private SchedulerConfig schedulerConfig;

    @Value("${scheduler.re-create}")
    private boolean deleteOldSchedules;

    private JobService jobService;

    public SchedulerServiceImpl(@Autowired JobService jobService, SchedulerConfig schedulerConfig) {
        this.jobService = jobService;
        this.schedulerConfig = schedulerConfig;
        log.info("initialising scheduler");
    }

    @Override
    @PostConstruct
    public void scheduleCronJobs() throws WorkflowException {
        if (deleteOldSchedules) {
            schedulerConfig.getSchedules().stream().findAny().ifPresent(schedule -> {
                try {
                    jobService.cleanSchedules(AbstractJobSchedulerTask.CRON_GROUP);
                } catch (SchedulerException e) {
                    log.error("failed to remove schedules $0", e.getMessage());
                }
            });
        }
        log.info("scheduling cron jobs");
        Task[] tasks = schedulerConfig.getSchedules()
                .stream()
                .map(schedule ->  new AbstractJobSchedulerTask(jobService, schedule))
                .toArray(Task[]::new);
        String taskResult = new DefaultWorkflow<String>().execute(tasks, "false");
        log.info("completed scheduling job services $0", taskResult);
    }
}
