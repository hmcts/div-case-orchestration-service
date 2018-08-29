package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailClientTest {

    @Value("${uk.gov.notify.api.key}")
    private String apiKey;

    @Autowired
    private EmailClient emailClient;

    @Test
    public void shouldHaveTheCorrectAPIKey() {
        assertEquals(apiKey, emailClient.getApiKey());
    }

}
