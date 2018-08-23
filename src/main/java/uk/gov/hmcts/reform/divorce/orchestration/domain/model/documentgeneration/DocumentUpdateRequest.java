package uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DocumentUpdateRequest {
    private List<GeneratedDocumentInfo> documents;
    private Map<String, Object> caseData;
}
