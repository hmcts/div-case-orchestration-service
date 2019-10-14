package uk.gov.hmcts.reform.divorce.orchestration.domain.model.email;

import lombok.Value;

import java.util.Map;

@Value
public final class GenericEmailContext {
    String destinationEmailAddress;
    EmailTemplateNames templateId;
    Map<String, String> templateFields;
}
