package uk.gov.hmcts.reform.divorce.orchestration.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Schedule {

    private String name;

    private String description;

    private Class jobClass;

    private String cron;
}
