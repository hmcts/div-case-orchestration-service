package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CaseOrchestrationValues {

    @Value("${aos-overdue.grace-period}")
    private String aosOverdueGracePeriod;

    public String getAosOverdueGracePeriod() {
        return aosOverdueGracePeriod;
    }

}