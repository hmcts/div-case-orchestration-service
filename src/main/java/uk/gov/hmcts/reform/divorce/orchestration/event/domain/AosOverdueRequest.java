package uk.gov.hmcts.reform.divorce.orchestration.event.domain;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event requesting a case to be moved to AosOverdue state.
 */
@Getter
public class AosOverdueRequest extends ApplicationEvent {

    private String caseId;

    public AosOverdueRequest(Object source, String caseId) {
        super(source);
        this.caseId = caseId;
    }

}