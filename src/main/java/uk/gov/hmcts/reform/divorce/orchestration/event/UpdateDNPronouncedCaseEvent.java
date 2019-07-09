package uk.gov.hmcts.reform.divorce.orchestration.event;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.context.ApplicationEvent;

@Value
@EqualsAndHashCode(callSuper = true)
public class UpdateDNPronouncedCaseEvent extends ApplicationEvent {
    private final transient String authToken;
    private final transient String caseId;

    public UpdateDNPronouncedCaseEvent(Object source, String authToken, String caseId) {
        super(source);

        this.authToken = authToken;
        this.caseId = caseId;
    }
}
