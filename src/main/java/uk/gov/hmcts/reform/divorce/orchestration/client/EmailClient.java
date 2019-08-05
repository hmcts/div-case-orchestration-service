package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.service.notify.NotificationClient;

@Component
public class EmailClient extends NotificationClient {

    @Autowired
    public EmailClient(@Value("${uk.gov.notify.api.key}") String apiKey, @Value("${uk.gov.notify.api.baseUrl}") final String baseUrl) {
        super(apiKey, baseUrl);
    }
}
