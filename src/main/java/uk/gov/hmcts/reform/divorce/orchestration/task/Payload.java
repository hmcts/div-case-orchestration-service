package uk.gov.hmcts.reform.divorce.orchestration.task;

@FunctionalInterface
public interface Payload<T> {
    
    T getBody();
}
