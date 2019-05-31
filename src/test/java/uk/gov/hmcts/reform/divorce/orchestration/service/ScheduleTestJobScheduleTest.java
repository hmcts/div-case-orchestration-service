package uk.gov.hmcts.reform.divorce.orchestration.service;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.scheduler.model.JobData;
import uk.gov.hmcts.reform.divorce.scheduler.service.JobService;

import java.time.ZonedDateTime;
import java.util.Collections;


@RunWith(SpringRunner.class)
@SpringBootTest
public class ScheduleTestJobScheduleTest {

    @Autowired
    JobService jobService;

    @Test
    public void testRun() {
        Assert.assertNotNull("expect 0==0 ", jobService);
        JobData jobData = JobData.builder()
                .jobClass(TestJob.class)
                .id("test")
                .data(Collections.emptyMap())
                .description("desc")
                .build();
        JobKey jobKey = jobService.scheduleJob(jobData, ZonedDateTime.now());
    }
}
