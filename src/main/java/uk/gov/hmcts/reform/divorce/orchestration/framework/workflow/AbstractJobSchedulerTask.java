package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.models.Schedule;
import uk.gov.hmcts.reform.divorce.scheduler.model.JobData;
import uk.gov.hmcts.reform.divorce.scheduler.services.JobService;

import java.util.Collections;
import java.util.UUID;

@Slf4j
public class AbstractJobSchedulerTask implements Task<String> {

    public static final String CRON_GROUP = "NIGHTLY_CRON";

    private JobService jobService;

    private Schedule schedule;

    public AbstractJobSchedulerTask(JobService jobService, Schedule schedule) {
        this.jobService = jobService;
        this.schedule = schedule;
    }

    @Override
    public String execute(TaskContext context, String payload) throws TaskException {
        log.info("creating a job data and scheduling a job ");
        JobData jobData = JobData.builder().id(UUID.randomUUID().toString())
                .id(schedule.getName())
                .description(schedule.getDescription())
                .group(CRON_GROUP)
                .jobClass(schedule.getJobClass())
                .data(Collections.emptyMap()).build();
        JobKey jobKey = jobService.scheduleJob(jobData, schedule.getCron());
        return jobKey.getName();
    }
}
