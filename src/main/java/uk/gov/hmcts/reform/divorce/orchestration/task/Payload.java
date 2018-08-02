package uk.gov.hmcts.reform.divorce.orchestration.task;

public interface Payload<T> {
    
    T getBody();
}
