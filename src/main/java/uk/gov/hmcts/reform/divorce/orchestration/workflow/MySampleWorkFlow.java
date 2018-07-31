package uk.gov.hmcts.reform.divorce.orchestration.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class MySampleWorkFlow {

public static void main(String args[]) {

       Workflow workFlow = new DefaultWorkflow();
        try {
            //example 1
            Payload payload = workFlow.execute(
                new Task[] {(x) -> ()-> x.getBody() + " Hello ",
                    (x) -> ()-> x.getBody() + " World",
                    (x) -> ()-> x .getBody() + " Example"}, () -> ""
            );

            log.info(payload.getBody().toString());

            //example2
            payload = workFlow.execute(
                new Task[] {new HelloTask(), new WorldTask(), (x) -> ()-> x.getBody() + " Example"}, () -> "Mr "
            );
            log.info(payload.getBody().toString());
        } catch (WorkflowException e) {
            log.info(e.getMessage());
        }

    }
}

class DefaultRestCallTask implements  Task {

    @Autowired
    RestTemplate restTemplate;

    String url ;

    public DefaultRestCallTask(String url) {
       this.url = url;
    }


    @Override
    public Payload execute(Payload in) throws TaskException {
        return null;
    }
}

class HelloTask implements Task {

    @Override
    public Payload execute(Payload in) throws TaskException {
        return () -> in.getBody() + "Hello";
    }
}


class WorldTask implements Task {

    @Override
    public Payload execute(Payload in) throws TaskException {
        return () -> in.getBody() + " World";
    }
}
