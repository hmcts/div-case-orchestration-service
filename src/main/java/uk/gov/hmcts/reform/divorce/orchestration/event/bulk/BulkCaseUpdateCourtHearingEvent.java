package uk.gov.hmcts.reform.divorce.orchestration.event.bulk;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
public class BulkCaseUpdateCourtHearingEvent extends ApplicationEvent {
    private final transient Map<String, Object> caseDetails;

    public BulkCaseUpdateCourtHearingEvent(Object source,  Map<String, Object> caseDetails) {
        super(source);

        this.caseDetails = caseDetails;
    }
}
