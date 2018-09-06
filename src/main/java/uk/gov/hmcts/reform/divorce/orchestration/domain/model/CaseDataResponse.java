package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class CaseDataResponse {
    private String caseId;
    private String courts;
    private String state;
    private Map<String, Object> data;
}
