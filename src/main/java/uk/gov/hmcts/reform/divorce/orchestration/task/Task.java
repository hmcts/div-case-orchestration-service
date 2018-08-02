package uk.gov.hmcts.reform.divorce.orchestration.task;

@FunctionalInterface
public interface Task {

    Payload execute(Payload in) throws TaskException;

}
