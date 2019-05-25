package uk.gov.hmcts.reform.divorce.scheduler.services;

import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.scheduler.config.SchedulerConfig;
import uk.gov.hmcts.reform.divorce.scheduler.model.JobData;
import uk.gov.hmcts.reform.divorce.scheduler.model.Schedule;

import java.util.Collections;
import java.util.UUID;
import javax.annotation.PostConstruct;

@EnableConfigurationProperties(SchedulerConfig.class)
@Component
@Slf4j
public class CronJobScheduler {

    @Value("${scheduler.re-create}")
    private boolean deleteOldSchedules;

    private final SchedulerConfig schedulerConfig;

    private final JobService jobService;

    public CronJobScheduler(@Autowired JobService jobService, SchedulerConfig schedulerConfig) {
        this.jobService = jobService;
        this.schedulerConfig = schedulerConfig;
        log.info("initialising scheduler");
    }

    @PostConstruct
    public void scheduleCronJobs() {
        if (deleteOldSchedules) {
            schedulerConfig.getSchedules().stream().findAny().ifPresent(schedule -> {
                try {
                    jobService.cleanSchedules(schedule.getCronGroup());
                } catch (SchedulerException e) {
                    log.error("failed to remove schedules {}", e.getMessage());
                }
            });
        }
        log.info("scheduling cron jobs");

        for (Schedule schedule: schedulerConfig.getSchedules()) {
            jobService.scheduleJob(buildJobData(schedule), schedule.getCron());
            log.info("completed scheduling job services {}", schedule.getName());
        }
    }

    private JobData buildJobData(Schedule schedule) {
        return JobData.builder().id(UUID.randomUUID().toString())
            .id(schedule.getName())
            .description(schedule.getDescription())
            .group(schedule.getCronGroup())
            .jobClass(schedule.getJobClass())
            .data(Collections.emptyMap()).build();
    }
}
