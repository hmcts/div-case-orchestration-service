package uk.gov.hmcts.reform.divorce.orchestration.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.mapper.config.DataFormatterConfiguration;

@Component
@Slf4j
public class CaseDataFormatterConfiguration implements DataFormatterConfiguration {

    private final String documentManagementStoreUrl;
    private final String cohort;

    public CaseDataFormatterConfiguration(@Value("${document.management.store.url}") String documentManagementStoreUrl,
                                          @Value("${cohort}") String cohort) {
        this.documentManagementStoreUrl = documentManagementStoreUrl;
        log.info("DM store url {}", this.documentManagementStoreUrl);

        this.cohort = cohort;
    }

    @Override
    public String getDocumentManagementStoreUrl() {
        return documentManagementStoreUrl;
    }

    @Override
    public String getCohort() {
        return cohort;
    }

}
