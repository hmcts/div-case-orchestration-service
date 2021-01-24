package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ccd.data-store.api")
@Getter
public class CcdDataStoreServiceConfiguration {
    private String removeCaseRolesUrl;
}
