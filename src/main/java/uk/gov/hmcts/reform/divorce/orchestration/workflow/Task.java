package uk.gov.hmcts.reform.divorce.orchestration.workflow;

@FunctionalInterface
public interface Task {

    Payload execute(Payload in) throws TaskException;
}
