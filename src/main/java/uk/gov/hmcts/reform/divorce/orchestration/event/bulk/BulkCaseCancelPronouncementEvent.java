package uk.gov.hmcts.reform.divorce.orchestration.event.bulk;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.context.ApplicationEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
public class BulkCaseCancelPronouncementEvent extends ApplicationEvent {
    private final transient Map<String, Object> caseDetails;

    public BulkCaseCancelPronouncementEvent(Object source, Map<String, Object> caseDetails) {
        super(source);

        this.caseDetails = caseDetails;
    }
}
