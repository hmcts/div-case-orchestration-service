package uk.gov.hmcts.reform.divorce.orchestration.domain.model.email;

import lombok.Value;

import java.util.Map;

@Value
public final class EmailToSend {
    String emailAddress;
    String templateId;
    Map<String, String> templateFields;
    String referenceId;
}
