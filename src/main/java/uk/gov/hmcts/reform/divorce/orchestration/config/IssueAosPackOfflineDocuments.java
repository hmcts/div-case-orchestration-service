package uk.gov.hmcts.reform.divorce.orchestration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts;
import uk.gov.hmcts.reform.divorce.orchestration.util.DocumentGenerator;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties
public class IssueAosPackOfflineDocuments {
    private Map<DivorceFacts, DocumentGenerator> issueAosPackOffLine;
}
