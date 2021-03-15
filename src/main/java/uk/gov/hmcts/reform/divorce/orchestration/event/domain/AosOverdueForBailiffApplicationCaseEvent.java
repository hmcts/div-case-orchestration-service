package uk.gov.hmcts.reform.divorce.orchestration.event.domain;

/**
 * Event representing that the AOS for a case served by bailiff method is now overdue.
 */
public class AosOverdueForBailiffApplicationCaseEvent extends AosOverdueEvent {

    public AosOverdueForBailiffApplicationCaseEvent(Object source, String caseId) {
        super(source, caseId);
    }

}