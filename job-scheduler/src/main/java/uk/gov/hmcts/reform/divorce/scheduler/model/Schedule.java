package uk.gov.hmcts.reform.divorce.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.quartz.Job;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Schedule {

    private String name;

    private String description;

    private Class<? extends Job> jobClass;

    private String cronGroup;

    private String cron;

    private boolean enabled;
}
