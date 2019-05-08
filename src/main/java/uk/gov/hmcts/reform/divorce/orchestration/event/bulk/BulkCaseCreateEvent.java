package uk.gov.hmcts.reform.divorce.orchestration.event.bulk;

import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Data
public class BulkCaseCreateEvent extends ApplicationEvent {
    private final transient Map<String, Object> caseDetails;

    public BulkCaseCreateEvent(Object source,  Map<String, Object> caseDetails) {
        super(source);
        this.caseDetails = caseDetails;
    }
}
