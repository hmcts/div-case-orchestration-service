package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aca.api")
public class AssignCaseAccessServiceConfiguration {
    private String caseAssignmentsUrl;
}
