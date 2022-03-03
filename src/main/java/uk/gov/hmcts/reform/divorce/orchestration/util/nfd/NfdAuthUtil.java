package uk.gov.hmcts.reform.divorce.orchestration.util.nfd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NfdAuthUtil {

    @Value("${idam.caseworker.username}")
    private String caseworkerUserName;

    @Value("${idam.caseworker.password}")
    private String caseworkerPassword;

    private final NfdIdamClient idamClient;

    public String getCaseworkerToken() {
        log.info("Get caseworker tkn for {}", caseworkerUserName);
        return getIdamOauth2Token(caseworkerUserName, caseworkerPassword);
    }

    private String getIdamOauth2Token(String username, String password) {
        return idamClient.authenticateUser(username, password);
    }


}
