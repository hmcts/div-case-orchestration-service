package uk.gov.hmcts.reform.divorce.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.scheduler.exception.JobException;
import uk.gov.hmcts.reform.divorce.scheduler.model.JobData;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Set;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final Scheduler scheduler;

    public JobKey scheduleJob(JobData jobData, ZonedDateTime startDateTime) {
        try {
            scheduler.scheduleJob(
                getJobDetail(jobData),
                newTrigger()
                    .startAt(Date.from(startDateTime.toInstant()))
                    .withIdentity(jobData.getId(), jobData.getGroup())
                    .withDescription(jobData.getDescription())
                    .withSchedule(
                        simpleSchedule()
                            .withMisfireHandlingInstructionNowWithExistingCount()
                    )
                    .build()
            );

            return getJobKey(jobData);
        } catch (SchedulerException exc) {
            throw new JobException("Error while scheduling a job", exc);
        }
    }

    public JobKey scheduleJob(JobData jobData, String cronExpression) {
        try {
            scheduler.scheduleJob(
                getJobDetail(jobData),
                newTrigger()
                    .withIdentity(jobData.getId(), jobData.getGroup())
                    .withDescription(jobData.getDescription())
                    .withSchedule(
                        cronSchedule(cronExpression)
                    )
                    .build()
            );
            return getJobKey(jobData);
        } catch (SchedulerException exc) {
            throw new JobException("Error while scheduling a job", exc);
        }
    }

    public void cleanSchedules(String... scheduleGroups) throws JobException {

        for (String group : scheduleGroups) {
            try {
                Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(group));
                for (JobKey jobKey : jobKeys) {
                    scheduler.deleteJob(jobKey);
                }
            } catch (SchedulerException e) {
                throw new JobException(String.format("Error cleaning group %s", group), e);
            }
        }
    }

    public JobKey rescheduleJob(JobData jobData, ZonedDateTime startDateTime) {

        try {

            TriggerKey triggerKey = triggerKey(jobData.getId(), jobData.getGroup());
            SimpleTrigger newTrigger = newTrigger()
                .startAt(Date.from(startDateTime.toInstant()))
                .withIdentity(jobData.getId(), jobData.getGroup())
                .withDescription(jobData.getDescription())
                .withSchedule(
                    simpleSchedule()
                        .withMisfireHandlingInstructionNowWithExistingCount()
                )
                .build();

            Date rescheduleJob = scheduler.rescheduleJob(triggerKey, newTrigger);

            if (rescheduleJob == null) {
                scheduleJob(jobData, startDateTime);
            }

            return getJobKey(jobData);

        } catch (SchedulerException exc) {
            throw new JobException("Error while rescheduling a job", exc);
        }
    }

    private JobKey getJobKey(JobData jobData) {
        return JobKey.jobKey(jobData.getId(), jobData.getGroup());
    }

    private JobDetail getJobDetail(JobData jobData) {
        return newJob(jobData.getJobClass())
            .withIdentity(jobData.getId(), jobData.getGroup())
            .withDescription(jobData.getDescription())
            .usingJobData(new JobDataMap(jobData.getData()))
            .requestRecovery()
            .build();

    }
}
