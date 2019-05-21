package uk.gov.hmcts.reform.divorce.orchestration.models;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class Schedule {

    private String name;

    private String description;

    private Class jobClass;

    private  String cron;
}
