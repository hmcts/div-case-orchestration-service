package uk.gov.hmcts.reform.divorce.orchestration.util.nfd;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class NfdOAuth2Configuration {

    private String clientId;
    private String redirectUri;
    private String clientSecret;

    @Autowired
    public NfdOAuth2Configuration(
        @Value("${idam.xui.client.redirect_uri:}") String redirectUri,
        @Value("${idam.xui.client.id:}") String clientId,
        @Value("${idam.xui.client.secret:}") String clientSecret
    ) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.clientSecret = clientSecret;
    }
}
