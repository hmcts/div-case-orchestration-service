package uk.gov.hmcts.reform.divorce.scheduler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.divorce.scheduler.model.Schedule;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("scheduler")
@Data
@Configuration
public class SchedulerConfig {

    private final List<Schedule> schedules = new ArrayList<>();

}
