package uk.gov.hmcts.reform.divorce.orchestration.event.domain;

/**
 * Event representing that the AOS for a case served by a process server is now overdue.
 */
public class AosOverdueForProcessServerCaseEvent extends AosOverdueEvent {

    public AosOverdueForProcessServerCaseEvent(Object source, String caseId) {
        super(source, caseId);
    }

}