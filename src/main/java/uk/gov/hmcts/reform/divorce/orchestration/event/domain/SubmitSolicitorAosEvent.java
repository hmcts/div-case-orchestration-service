package uk.gov.hmcts.reform.divorce.orchestration.event.domain;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.context.ApplicationEvent;

@Value
@EqualsAndHashCode(callSuper = true)
public class SubmitSolicitorAosEvent extends ApplicationEvent {
    public SubmitSolicitorAosEvent(Object source) {
        super(source);
    }
}