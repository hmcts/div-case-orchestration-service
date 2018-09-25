package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinkRespondentRequest {
    private String caseId;
    private String pin;
}
