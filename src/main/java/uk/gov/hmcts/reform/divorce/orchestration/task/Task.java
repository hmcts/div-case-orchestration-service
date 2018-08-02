package uk.gov.hmcts.reform.divorce.orchestration.task;

import org.springframework.stereotype.Component;

@FunctionalInterface
@Component
public interface Task {
    Payload execute(Payload in) throws TaskException;
}