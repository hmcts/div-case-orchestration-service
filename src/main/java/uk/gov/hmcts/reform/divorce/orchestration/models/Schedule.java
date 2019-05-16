package uk.gov.hmcts.reform.divorce.orchestration.models;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Schedule {

    private String name;

    private String description;

    private String jobClass;

    private  String cron;
}
