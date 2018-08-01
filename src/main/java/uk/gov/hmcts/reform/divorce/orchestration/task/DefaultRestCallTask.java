package uk.gov.hmcts.reform.divorce.orchestration.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class DefaultRestCallTask implements Task {

    @Autowired
    RestTemplate restTemplate;

    String url;

    public DefaultRestCallTask(String url) {
        this.url = url;
    }

    @Override
    public Payload execute(Payload in) throws TaskException {
        return null;
    }
}