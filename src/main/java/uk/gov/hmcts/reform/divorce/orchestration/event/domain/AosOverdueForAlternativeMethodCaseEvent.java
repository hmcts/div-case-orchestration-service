package uk.gov.hmcts.reform.divorce.orchestration.event.domain;

/**
 * Event representing that the AOS for a case served by alternative method is now overdue.
 */
public class AosOverdueForAlternativeMethodCaseEvent extends AosOverdueEvent {

    public AosOverdueForAlternativeMethodCaseEvent(Object source, String caseId) {
        super(source, caseId);
    }

}