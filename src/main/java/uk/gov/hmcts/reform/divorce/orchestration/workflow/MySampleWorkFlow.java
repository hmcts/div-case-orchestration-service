package uk.gov.hmcts.reform.divorce.orchestration.workflow;

import lombok.extern.slf4j.Slf4j;

import uk.gov.hmcts.reform.divorce.orchestration.task.HelloTask;
import uk.gov.hmcts.reform.divorce.orchestration.task.Payload;
import uk.gov.hmcts.reform.divorce.orchestration.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.task.WorldTask;

@Slf4j
public class MySampleWorkFlow {

    public static void main(String[] args) {

        Workflow workFlow = new DefaultWorkflow();
        
        try {
            // example 1
            Payload payload = workFlow.execute(
                new Task[] {
                    (in) -> () -> in.getBody() + " Hello ",
                    (in) -> () -> in.getBody() + " World",
                    (in) -> () -> in.getBody() + " Example"
                }, () -> ""
            );

            log.info(payload.getBody().toString());

            // example 2
            payload = workFlow.execute(
                new Task[] {
                    new HelloTask(),
                    new WorldTask(),
                    (in) -> () -> in.getBody() + " Example"
                }, () -> "Mr "
            );

            log.info(payload.getBody().toString());
        } catch (WorkflowException e) {
            log.info(e.getMessage());
        }
    }
}
