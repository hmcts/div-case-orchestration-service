package uk.gov.hmcts.reform.divorce.orchestration.task;

public class WorldTask implements Task {

    @Override
    public Payload execute(Payload in) throws TaskException {
        return () -> in.getBody() + " World";
    }
}