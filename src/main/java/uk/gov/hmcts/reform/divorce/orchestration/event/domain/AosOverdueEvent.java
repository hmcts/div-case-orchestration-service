package uk.gov.hmcts.reform.divorce.orchestration.event.domain;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event representing that a case is to be moved to AosOverdue state.
 */
@Getter
public class AosOverdueEvent extends ApplicationEvent {

    private String caseId;

    public AosOverdueEvent(Object source, String caseId) {
        super(source);
        this.caseId = caseId;
    }

}