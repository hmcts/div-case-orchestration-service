package uk.gov.hmcts.reform.divorce.orchestration.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.divorce.orchestration.models.Schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties("scheduler")
@Data
@Configuration
public class SchedulerConfig {

    private final List<Schedule> schedules = new ArrayList<>();
}
