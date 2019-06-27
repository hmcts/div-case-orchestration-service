package uk.gov.hmcts.reform.divorce.orchestration.event;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.context.ApplicationEvent;

@Value
@EqualsAndHashCode(callSuper = true)
public class CleanStatusEvent extends ApplicationEvent {
    public CleanStatusEvent(Object source) {
        super(source);
    }
}