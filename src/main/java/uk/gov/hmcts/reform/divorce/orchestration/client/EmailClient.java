package uk.gov.hmcts.reform.divorce.orchestration.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.service.notify.NotificationClient;

@Slf4j
@Component
public class EmailClient extends NotificationClient {

    @Autowired
    public EmailClient(@Value("${uk.gov.notify.api.key}") String apiKey, @Value("${uk.gov.notify.api.baseUrl}") final String baseUrl) {
        super(apiKey, baseUrl);
        log.info("apikey------------------------- {}", apiKey);
    }
}
