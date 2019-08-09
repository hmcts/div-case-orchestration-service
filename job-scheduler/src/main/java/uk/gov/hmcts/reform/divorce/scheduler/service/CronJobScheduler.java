package uk.gov.hmcts.reform.divorce.scheduler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.scheduler.config.SchedulerConfig;
import uk.gov.hmcts.reform.divorce.scheduler.model.JobData;
import uk.gov.hmcts.reform.divorce.scheduler.model.Schedule;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

@EnableConfigurationProperties(SchedulerConfig.class)
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
@Component
@Slf4j
public class CronJobScheduler {

    @Value("${scheduler.recreate}")
    private boolean reCreateSchedules;

    private final SchedulerConfig schedulerConfig;

    private final JobService jobService;

    public CronJobScheduler(@Autowired JobService jobService, SchedulerConfig schedulerConfig) {
        this.jobService = jobService;
        this.schedulerConfig = schedulerConfig;
        log.info("initialising scheduler");
    }

    @PostConstruct
    public void scheduleCronJobs() {
        if (reCreateSchedules) {
            Set<String> cronGroups = schedulerConfig.getSchedules()
                .stream()
                .map(Schedule::getCronGroup)
                .collect(Collectors.toSet());

            for (String cronGroup : cronGroups) {
                jobService.cleanSchedules(cronGroup);
                log.info("Cron group {} deleted", cronGroup);
            }
        }
        log.info("scheduling cron jobs");

        schedulerConfig.getSchedules()
            .stream()
            .filter(Schedule::isEnabled)
            .forEach(schedule -> {
                jobService.scheduleJob(buildJobData(schedule), schedule.getCron());
                log.info("completed scheduling job service {} with cron {}", schedule.getName(), schedule.getCron());
            });
        log.info("All cron job scheduled");
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