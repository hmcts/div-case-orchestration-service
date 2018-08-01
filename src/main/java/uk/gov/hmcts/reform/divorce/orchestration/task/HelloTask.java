package uk.gov.hmcts.reform.divorce.orchestration.task;

public class HelloTask implements Task {

    @Override
    public Payload execute(Payload in) throws TaskException {
        return () -> in.getBody() + "Hello";
    }
}