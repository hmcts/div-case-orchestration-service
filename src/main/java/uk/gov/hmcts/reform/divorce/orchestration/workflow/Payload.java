package uk.gov.hmcts.reform.divorce.orchestration.workflow;

@FunctionalInterface
public interface Payload<T> {

   T getBody();
}
