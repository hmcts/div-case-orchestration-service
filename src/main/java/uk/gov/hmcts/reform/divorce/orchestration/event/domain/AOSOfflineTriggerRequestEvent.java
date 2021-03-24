package uk.gov.hmcts.reform.divorce.orchestration.event.domain;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class AOSOfflineTriggerRequestEvent extends ApplicationEvent {

    @Getter
    private final String caseId;

    public AOSOfflineTriggerRequestEvent(Object source, String caseId) {
        super(source);
        this.caseId = caseId;
    }

}